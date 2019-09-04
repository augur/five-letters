db.userData.createIndex( { "login": 1 }, { unique: true } )

db.letter.createIndex(
    {
        "login": 1,
        "read": 1,
        "openDate": 1
    },
    { unique: false }
)