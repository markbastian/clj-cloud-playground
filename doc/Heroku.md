## Heroku

Heroku is perhaps the easiest way to deploy an app. Simply create the app and push your code. Use heroku port forwarding to enable a remote repl.

#### Create the App
1. [Install the tools](https://devcenter.heroku.com/articles/getting-started-with-clojure#set-up)
1. Create an app (executable jar).
1. [Deploy the Application](https://devcenter.heroku.com/articles/getting-started-with-clojure#deploy-the-app)
   1. `heroku create`
   1. `git push heroku master`
   1. `heroku ps:scale web=1` to ensure running
   1. `heroku open` to launch the app

1. Optional: [Use a Procfile](https://devcenter.heroku.com/articles/getting-started-with-clojure#define-a-procfile) to explicitly declare the application run command.
1. Use `heroku ps:forward 3001' (Assuming your repl server is on port 3001) to [forward your local port to your remote repl port.](https://devcenter.heroku.com/articles/exec#port-forwarding)

#### Tips

[View logs](https://devcenter.heroku.com/articles/getting-started-with-clojure#view-logs) with `heroku logs --tail`

Stop your app using [`heroku ps:scale web=0`](https://devcenter.heroku.com/articles/getting-started-with-clojure#scale-the-app) to take the number of instances to 0.

`heroku apps:destroy` can be used to completely destroy your app.
