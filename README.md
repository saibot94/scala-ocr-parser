# licenta-ocr-parser

A barebones Scala app (using the Play framework), which can easily be deployed to Heroku. 

# Purpose 

This application should take an image received in a POST, normalize it (tilting, contrast, grayscale) and then apply an
Optical Character Recognition algorithm on the image so that all of the sentences are obtained. Future step: sentiment analysis on the content of the text that is parsed.

This application support the [Getting Started with Scala/Play on Heroku](https://devcenter.heroku.com/articles/getting-started-with-scala) article - check it out.

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

