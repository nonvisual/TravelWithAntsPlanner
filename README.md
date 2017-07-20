# TravelWithAntsPlanner

Travel with Ants Planner (TAP)
Solution to one of the interview challenges.

The app uses a variant of Ant Colony Optimization algorithm, which helps to plan a close to optimal travel route. It also uses Google Maps for plotting delivered route and visited sites.

As input a location is given as LONG/LAT and you have a perfect helicopter that travels 80km/h on average with unlimited fuel.
The traveller visits only UNESCO World Heritage places (which are available online), of which there are three types: cultura l places, such as historic city centres,
natural places, such as wildlife sanctuaries, and mixed places that represent both. The task is to visit as many sites as you can, with an equal number of cultural and natural world heritage
sites.

Travel location data: There are over 1,000 UNESCO World Heritage sites in the world. 
Their description, including their geographic coordinates, is in an Excel file which
can be downloaded from http://whc.unesco.org/en/list/xls/?2016 .

The app is available as jar class for console execution:
1.	Prepare the database									 Execute Database preparation script: 							     mysql -u username -p database_name < PrepareDBScript.sql
2.	Run the jar file 										        Use initialization parameter, which specifies how rich your travel graph should be. The parameter is maximum hours distance, which should be included in the database. The maximum value allows for complete graph (a lot of redundant information), while small values may produce disconnected graph. Recommended value: 40 hours				        java -jar TravelProject-0.0.1-SNAPSHOT-jar-with-dependencies.jar 40
3.	Specify starting point latitude and longitude
4.	Enter your travel length in hours (3 weeks = 3 * 7 * 24= 504 hours)
5.	Specify optimization algorithm timeout in seconds	
6.	Specify output file name (*.html) if needed						            It should be a legal destination. In the end of the program, if option is used, the browser will open with google maps with all travel points		

