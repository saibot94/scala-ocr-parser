# licenta-ocr-parser

A barebones Scala app (using the Play framework), which can easily be deployed to Heroku. 

## Purpose 

This application should take an image received in a POST, normalize it (tilting, contrast, grayscale) and then apply an
Optical Character Recognition algorithm on the image so that all of the sentences are obtained. Future step: sentiment analysis on the content of the text that is parsed.

This application support the [Getting Started with Scala/Play on Heroku](https://devcenter.heroku.com/articles/getting-started-with-scala) article - check it out.

## Usage (example Python client)

The below example shows how to use Python in order to post an image and then receive back the content.

The resulting json contains "text", which is the text extracted and "image", which is the preprocessed image with
bounding boxes drawn on it.

```python
>>> import requests
>>> files = {'picture': open('bcrfile.jpg', 'rb')}
>>> url = 'https://licenta-ocr-parser.herokuapp.com/upload'
>>> r = requests.post(url,files=files)
>>> r
<Response [200]>
>>> r.json()['text']
'SOCIETATE ADMINISTRATA  SISTEM DUALIST \n Regina Elisabeta   Sector  Bucuresti, cod 030016 \nInmatriculata  Registrul Comertului: J40/90/1991 \nInmatriculata  Registrul Bancar  RB-PJR-40-008/18.02.1999 \nCod Unic  lnregistrare:  361757 \nlnregistrata  Registrul  evidenta  prelucrarilor de date  ca racter personal sub  3776  377 \nCapital Social:  62534161450 lei \nSWIFT: RNCB   Site: www.bcr.ro; Email: contact.center@bcr.ro \nWOBCRT 0800.801 .BCR (0800801227), apelabil gratuit din orice retea nationala; \n+4021 407   apelabil din strainatate  tarif normal. \n efectuarea  operatiuni  carduri bancare   1667019 \n'
```

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

or

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## Documentation

For more information about using Play and Scala on Heroku, see these Dev Center articles:

- [Play and Scala on Heroku](https://devcenter.heroku.com/categories/language-support#scala-and-play)

