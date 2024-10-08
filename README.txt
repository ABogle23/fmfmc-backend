Project Setup Instructions

Prerequisites:
- Java Development Kit: JDK 17 or higher installed.
- Gradle
- MySQL Server: Ensure you have a MySQL server running (locally or hosted) w/ MySQL 8.0 or above.
- API Keys for the following: OpenChargeMap, OpenRouteService, Mapbox and Foursquare.

Setup Instructions

1. Clone the Repository
'git clone git@github.com:ABogle23/fmfmc-backend.git'

2. Configure the application-template.properties file
- Add your API keys to the application-template.properties file located in the resources folder under main. Rename the file to application.properties.
- Add your MySQL database connection details to the application.properties file.
- Set an API key for the application. This can be any string value.
- Set 'evscraperservice.base-url' to the relevant site URL for web scraping.
- Rename the application-template.properties file to application.properties.

3. Run the application
- Via an IDE or the command line, run the application. The application will start on port 8080 by default.
- The application will create the necessary empty tables in the database on startup.
- Via an IDE or the command line, execute the following commands to populate the database '--updateChargers' and optionally '--runScraper'.
-- '--updateChargers' will update the database with the latest charger data from OpenChargeMap.
-- '--runScraper' will run the web scraper to populate the database with the latest EV data from the web.

4. Test the application
- Using a tool like Postman, you can test the API endpoints, ensure the application API key is contained in the request headers.
- The OpenAPI docs are available at 'api/api-docs-yaml' and 'api/api-docs-json'. '/swagger-ui.html' for interactive docs.
