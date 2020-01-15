//db = db.getSiblingDB('five-letters')

// == 0.2 ==

db.timePeriod.save(
     {
         _id: "DAY",
         days: 1,
         weeks: 0,
         months: 0,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "THREE_DAYS",
         days: 3,
         weeks: 0,
         months: 0,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "WEEK",
         days: 0,
         weeks: 1,
         months: 0,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "TWO_WEEKS",
         days: 0,
         weeks: 2,
         months: 0,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "MONTH",
         days: 0,
         weeks: 0,
         months: 1,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "TWO_MONTHS",
         days: 0,
         weeks: 0,
         months: 2,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "FOUR_MONTHS",
         days: 0,
         weeks: 0,
         months: 4,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "SIX_MONTHS",
         days: 0,
         weeks: 0,
         months: 6,
         years: 0,
         enabled: true
     }
)
db.timePeriod.save(
     {
         _id: "YEAR",
         days: 0,
         weeks: 0,
         months: 0,
         years: 1,
         enabled: true
     }
)

// === 20.1.1 ===

db.job.save(
    {
        schedule:
            {
                nextExecutionTime: {"$date":{"$numberLong":"1579124100000"}},
                repeatMode: "ALWAYS",
                repeatInterval: {"$numberLong":"3600000"}
            },
        payload:
            {
                type: "dailyMailing",
                "data": ""
            },
        status: "ACTIVE"
    }
)

