# licenta-ocr-parser

A barebones Scala app (using the Play framework), which can easily be deployed to Heroku. 

## Purpose 

This application should take an image received in a POST, normalize it (tilting, contrast, grayscale) and then apply an
Optical Character Recognition algorithm on the image so that all of the sentences are obtained. It has dependency only on
Tesseract, the Tess4J library, and Log4J. Future step: sentiment analysis on the content of the text that is parsed.

The endpoint for uploading an image is __"/upload"__

You can also specify the following parameters in the body of the request:


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

The example below shows how to set drawing params on the resulting image. 

The three possible parameters are: 'drawrow', 'drawchar', 'drawword'. In case of their absence, the default is 'false'.

```python
>>> import requests
>>> files = {'picture' : open('bcrfile.jpg','rb')}
>>> import json
>>> params = {'drawrow': 'true'}
>>> response = requests.post('http://localhost:9000/upload', files=files,data=params)
>>> response
<Response [200]>
>>> response.json()['text']
'SOCIETATE ADMINISTRATA IN SISTEM DUALIST \r\nBd. Regina Elisabeta nr. 51 Sector 31 Bucuresti, cod 030016 \r\nInmatriculata Ia Registrul Comertului: J40/90/1991 \r\nInmatriculata la Registrul Bancar Nr. RB-PJR-40-008/18.02.1999 \r\nCod Unic de lnregistrare: RO 361757 \r\nlnregistrata la Registrul de evidenta a prelucrarilor de date CU ca racter personal sub nr. 3776 si 377 \r\nCapital Social: 4 62534161450 lei \r\nSWIFT: RNCB RO BU; Site: www.bcr.ro; Email: contact.center@bcr.ro \r\nWOBCRT 0800.801 .BCR (0800801227), apelabil gratuit din orice retea nationala; \r\n+4021 407 42 00 apelabil din strainatate Ia tarif normal. \r\ni efectuarea de operatiuni CU carduri bancare nr. l 1667019 \r\n'
```

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

or

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## Configuration

The application can be configured with the values set in the __models.config.AppConfig__ class.
The configurations are pretty self-explanatory.

## Documentation

For more information about using Play and Scala on Heroku, see these Dev Center articles:

- [Play and Scala on Heroku](https://devcenter.heroku.com/categories/language-support#scala-and-play)

