//db = db.getSiblingDB('five-letters')

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