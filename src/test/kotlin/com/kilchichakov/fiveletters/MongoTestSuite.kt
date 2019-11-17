package com.kilchichakov.fiveletters

import com.kilchichakov.fiveletters.service.TransactionWrapper
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.KMongo
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder
import com.mongodb.BasicDBList
import org.bson.Document


open class MongoTestSuite {

    lateinit var db: MongoDatabase

    lateinit var transactionWrapper: TransactionWrapper

    private val initScript = MongoTestSuite::class.java.classLoader.getResource("mongo-init.js").readText()

    companion object {
        var replicaSetReady = false

        private var node1MongodExe: MongodExecutable? = null
        private var node1Mongod: MongodProcess? = null
        private var node2MongodExe: MongodExecutable? = null
        private var node2Mongod: MongodProcess? = null
        private var node3MongodExe: MongodExecutable? = null
        private var node3Mongod: MongodProcess? = null

        lateinit var client: MongoClient

        @BeforeAll
        @JvmStatic
        fun setUp() {
            val starter = MongodStarter.getDefaultInstance()

            replicaSetReady = false

            val bindIp = "localhost"

            val node1Port = 12345
            val node2Port = 12346
            val node3Port = 12347

//            val mongodConfig = MongodConfigBuilder()
//                    .version(Version.Main.PRODUCTION)
//                    .net(Net(bindIp, port, Network.localhostIsIPv6()))
//                    .build()
            val thread1 = Thread {
                node1MongodExe = starter.prepare(MongodConfigBuilder().version(Version.Main.V4_0)
                        .withLaunchArgument("--replSet", "rs0")
                        .cmdOptions(MongoCmdOptionsBuilder().useNoJournal(false).build())
                        .net(Net(bindIp, node1Port, Network.localhostIsIPv6()))
                        .build())

                node1Mongod = node1MongodExe?.start()
            }

            val thread2 = Thread {
                node2MongodExe = starter.prepare(MongodConfigBuilder().version(Version.Main.V4_0)
                        .withLaunchArgument("--replSet", "rs0")
                        .cmdOptions(MongoCmdOptionsBuilder().useNoJournal(false).build())
                        .net(Net(bindIp, node2Port, Network.localhostIsIPv6()))
                        .build())
                node2Mongod = node2MongodExe?.start()
            }

            val thread3 = Thread {
                node3MongodExe = starter.prepare(MongodConfigBuilder().version(Version.Main.V4_0)
                        .withLaunchArgument("--replSet", "rs0")
                        .cmdOptions(MongoCmdOptionsBuilder().useNoJournal(false).build())
                        .net(Net(bindIp, node3Port, Network.localhostIsIPv6()))
                        .build())
                node3Mongod = node3MongodExe?.start()
            }

            thread1.start()
            thread2.start()
            thread3.start()

            thread1.join()
            thread2.join()
            thread3.join()

            client = KMongo.createClient(bindIp, node1Port)

            val adminDatabase = client.getDatabase("admin")

            val config = Document("_id", "rs0")
            val members = BasicDBList()
            members.add(Document("_id", 0)
                    .append("host", "$bindIp:$node1Port"))
            members.add(Document("_id", 1)
                    .append("host", "$bindIp:$node2Port"))
            members.add(Document("_id", 2)
                    .append("host", "$bindIp:$node3Port"))
            config["members"] = members

            adminDatabase.runCommand(Document("replSetInitiate", config))
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            node1MongodExe?.stop()
            node1Mongod?.stop()
            node2MongodExe?.stop()
            node2Mongod?.stop()
            node3MongodExe?.stop()
            node3Mongod?.stop()
        }
    }

    @BeforeEach
    open fun setUpEach() {
        if (!replicaSetReady) {
            Thread.sleep(15000)
            replicaSetReady = true
        }
        transactionWrapper = TransactionWrapper(client)
        db = client.getDatabase("test")
        val script = BasicDBObject()
        script["eval"] = initScript
        db.runCommand(script)
    }

    @AfterEach
    fun tearDownEach() {
        client.dropDatabase("test")
    }

}