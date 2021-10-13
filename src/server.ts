import path from 'path';

import express, { Request, Response, RequestHandler } from 'express';
import hbs from 'express-handlebars';

const views = path.join(__dirname, 'templates');
const app = express();
app.set('view engine', 'html');
app.engine('html', hbs({
  extname: 'html',
}));
app.set('views', views);
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json() as RequestHandler);

app.get('/', (req: Request, res: Response) => {
  res.render('index.html');
});

// listen for requests :)
app.listen(process.env.PORT || 8080, () => {
  console.log(`Your app is listening on port ${process.env.PORT || 8080}`);
});
