# Versions descriptions

## 1.0.0
### API endpoints
#### equipments
+ GET /api/v1/equipments - equipments list
+ POST /api/v1/equipments - create new equipment
+ GET /api/v1/equipments/{number} - get single equipment

#### equipment sets
+ GET /api/v1/equipment-sets - equipment-sets list
+ POST /api/v1/equipment-sets - create new equipment-set
+ GET /api/v1/equipment-sets/{number} - get single equipment-set
+ PATCH /api/v1/equipment-sets/{number}/equipments - add equipments to set

#### blanks
+ GET /api/v1/blanks - blanks list
+ POST /api/v1/blanks - create new blanks
+ GET /api/v1/blanks/{number} - get single blanks



#### processes
+ GET /api/v1/processes - units list
+ POST /api/v1/processes - create new unit
+ GET /api/v1/processes/{number} - get single unit

#### processes-steps
+ GET /api/v1/processes/{number}/steps - list
+ POST /api/v1/processes/{number}/steps - add step with specific num
+ GET /api/v1/processes/{number}/steps/{number} - get specific steps
