# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html

import dateparser
import logging
import pymongo
import hashlib


class ViedemerdePipeline(object):

    collection_name = 'posts'

    def __init__(self, mongo_uri, mongo_db):
        self.mongo_uri = mongo_uri
        self.mongo_db = mongo_db
        self.md5_seen = set()

    @classmethod
    def from_crawler(cls, crawler):
        return cls(
            mongo_uri=crawler.settings.get('MONGO_URI'),
            mongo_db=crawler.settings.get('MONGO_DATABASE')
        )

    def open_spider(self, spider):
        self.client = pymongo.MongoClient(self.mongo_uri)
        self.db = self.client[self.mongo_db]

    def close_spider(self, spider):
        self.client.close()

    def process_item(self, item, spider):

        # convert date in date format
        try:
            item['date'] = dateparser.parse(item['date'], languages=['fr'])
        except:
            logging.error("Fail to convert in date format story: {}".format(item['url']))

        # insert in mongo. md5 makes sure to not have duplicate
        mongo_doc = dict(item)
        mongo_doc['md5'] = hashlib.md5(str(mongo_doc['content']).encode('utf-8')).hexdigest()
        if mongo_doc['md5'] not in self.md5_seen and spider.story_count < spider.max_stories:
            self.db[self.collection_name].update_one({'md5': mongo_doc['md5']}, {'$set': mongo_doc}, upsert=True)
            spider.story_count = spider.story_count + 1
            self.md5_seen.add(mongo_doc['md5'])

        return item
