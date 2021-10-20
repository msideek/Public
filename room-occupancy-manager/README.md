To run the project just import it as gradle project and run it as "Spring Boot App"

you can test API using postman using Get request "http://localhost:8080/api/rooms/{freePremiumRooms}/{freeEconomyRooms}" 

1- PotentialGuests as mock data in this path "\room-occupancy-manager\src\main\resources\PotentialGuests.json"

2- Test cases I covered 
	- list with PotentialGuests are paying more than or less EUR 100
		{	"guests": [23.0, 45.0, 155.0, 374.0, 22.0, 99.99, 100.0, 101.0, 115.0, 209.0] }
		
	- list with PotentialGuests are paying less than EUR 100
		{	"guests": [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0] }
		
	- list with PotentialGuests are paying more than EUR 100	
		{	"guests": [101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0, 110.0] }
		
	- empty list
		{	"guests": [] }	
		
3- Test cases result in this path "\room-occupancy-manager\Test-Result.txt"		