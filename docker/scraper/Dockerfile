FROM python:3

WORKDIR /usr/src/app

COPY ./scraper/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./scraper .

CMD ["scrapy", "crawl", "viedemerde"]
