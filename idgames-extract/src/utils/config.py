import json


class Config:

    def __init__(self):
        with open('config.json', 'r') as f:
            self.values: dict = json.load(f)

    def get(self, path: str):
        keys = path.split('.')

        value = self.values
        for key in keys:
            value = value[key]

        return value
