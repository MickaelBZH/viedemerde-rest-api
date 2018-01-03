# Vie de Merde REST API

This example uses the following:
* [MongoDB](https://www.mongodb.com/)
* [Scrapy (python)](https://scrapy.org/)
* [Play Framework (scala)](https://www.playframework.com/)
* [Docker Compose](https://www.docker.com/)

## Start and launch the application
```
docker-compose build
docker-compose up
```

```
http://localhost:9000/api/posts
http://localhost:9000/api/posts/[ID]
```

### Fetch the 200 newest posts from viedemerde.fr

To fetch the newest posts, you can run:

```
docker-compose run scraper scrapy crawl viedemerde
```

### Run API unit tests with scalatest

```
docker-compose run restapi sbt test
```

### Possible improvements

- Scraper parsing tests
- API tests with mock data
- Abstraction of DB call from the container
