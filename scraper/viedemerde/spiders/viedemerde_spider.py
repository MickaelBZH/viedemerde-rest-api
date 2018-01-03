import scrapy
from bs4 import BeautifulSoup
import re

from viedemerde.items import ViedemerdeItem


class VieDeMerdeSpider(scrapy.Spider):
    name = 'viedemerde'
    start_urls = ['http://www.viedemerde.fr/']
    page = 0
    story_count = 0
    max_stories = 200

    def parse(self, response):

        soup = BeautifulSoup(response.body, 'html.parser')

        main_articles = soup.find('div', class_='infinite-scroll')

        for article in main_articles.find_all('article'):
            item = ViedemerdeItem()
            # block content
            block = article.find('p', class_='block')
            if block:
                item['content'] = block.get_text()
                authordate_tag = article.find('div', class_='text-center')
                if authordate_tag:
                    # scrape the author and the date
                    m = re.match(r'\nPar (.+) -  /\n(.+) /\n', authordate_tag.get_text())
                    if m:
                        item['author'] = m.group(1)
                        item['date'] = m.group(2)
                        yield item

        if self.story_count < self.max_stories:
            self.page = self.page + 1
            # go to next page
            yield response.follow('?page={}'.format(self.page), self.parse)
