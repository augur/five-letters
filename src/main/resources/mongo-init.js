//db = db.getSiblingDB('five-letters')
db.userData.createIndex( { "login": 1 }, { unique: true } )

db.letter.createIndex(
    {
        "login": 1,
        "read": 1,
        "openDate": 1
    },
    { unique: false }
)

db.createCollection('systemState', { capped: true, size: 65536, max: 1 } )

db.passCode.createIndex(
    { "login": 1 },
    { unique: true, partialFilterExpression: { "login": { $exists: true } } }
)

db.timePeriod.createIndex(
    {
        "years": 1,
        "months": 1,
        "weeks": 1,
        "days": 1
    }
)