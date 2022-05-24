import pymorphy2
import re
import requests
from bs4 import BeautifulSoup as bs

morph = pymorphy2.MorphAnalyzer()

topics = []
topic_name = []


def pos(element):
    return morph.parse(element)[0].tag.POS


# parse text from topic
response = requests.get("https://habr.com/ru/company/ibm/blog/151191/")
html = response.text
soup = bs(html, "html.parser")
new_text = soup.body.get_text().strip()
text = []
# remove punctuation
words = re.sub(r'[^\w\s]', '', new_text)
words = words.replace('ё', 'е')
# to lower case
words = words.lower().split()
# normalize words and delete stop-words
functors_pos = {'INTJ', 'PRCL', 'CONJ', 'PREP', 'NPRO', 'NUMR', 'PRED'}
for word in words:
    if pos(word) not in functors_pos:
        normalized = morph.parse(word)[0].normal_form
        text.append(normalized)

# leaving only Russian words
r = re.compile("[а-яА-Я]+")
text = [w for w in filter(r.match, text)]


def create_topics(path, name):
    with open(path) as file:
        data = file.read().replace('ё', 'е').split()
        topic = [morph.parse(elem)[0].normal_form for elem in data]
        topics.append(topic)
        topic_name.append(name)


def jaccard_metric(topic):
    intersection = len([x for x in text if x in topic])
    union = len(topic) + len(text) - intersection
    return intersection / union


def cosine_metric(topic):
    union = list(set(text) | set(topic))

    vector1 = [0] * len(union)
    vector2 = [0] * len(union)

    for i in text:
        for j in range(len(union)):
            if i == union[j]:
                vector1[j] += 1

    for i in topic:
        for j in range(len(union)):
            if i == union[j]:
                vector2[j] += 1

    vector_mult = 0
    len1 = 0
    len2 = 0
    for i in range(len(union)):
        vector_mult = vector_mult + (vector1[i] * vector2[i])
        len1 = len1 + (vector1[i] ** 2)
        len2 = len2 + (vector2[i] ** 2)

    len1 = len1 ** 0.5
    len2 = len2 ** 0.5
    a = len1 * len2
    return vector_mult / a


# we read the file with words associations for the topic and bring it into normal form
create_topics("keywords/news.txt", "news")
create_topics("keywords/science.txt", "science")
create_topics("keywords/shopping.txt", "shopping")
create_topics("keywords/sport.txt", "sport")

# calculate the jaccard metric and the cosine metric between each topic and text
for i in range(len(topics)):
    print('jaccard metric for "%s": %.6f' % (topic_name[i], jaccard_metric(topics[i])))
    print('cosine metric for"%s": %.6f' % (topic_name[i], cosine_metric(topics[i])))
    print()
print(topics)