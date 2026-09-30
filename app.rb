require "sinatra"
require "dotenv/load"
require "net/http"
require "json"
require "securerandom"

CLIENT_ID = ENV.fetch("CLIENT_ID")
CLIENT_SECRET = ENV.fetch("CLIENT_SECRET")
CALLBACK_URL = ENV.fetch("CALLBACK_URL", "http://localhost:4567/github/callback")

enable :sessions
set :session_secret, ENV.fetch("SESSION_SECRET", SecureRandom.hex(32))

def parse_response(response)
  JSON.parse(response.body)
rescue JSON::ParserError
  {}
end

def exchange_code(code)
  params = {
    "client_id" => CLIENT_ID,
    "client_secret" => CLIENT_SECRET,
    "code" => code
  }

  uri = URI("https://github.com/login/oauth/access_token")
  request = Net::HTTP::Post.new(uri)
  request["Accept"] = "application/json"
  request["Content-Type"] = "application/x-www-form-urlencoded"
  request.body = URI.encode_www_form(params)

  result = Net::HTTP.start(uri.hostname, uri.port, use_ssl: true) do |http|
    http.request(request)
  end

  parse_response(result)
end

def user_info(token)
  uri = URI("https://api.github.com/user")
  request = Net::HTTP::Get.new(uri)
  request["Accept"] = "application/vnd.github+json"
  request["X-GitHub-Api-Version"] = "2022-11-28"
  request["Authorization"] = "Bearer #{token}"
  request["User-Agent"] = "Termux-Claude-Codex-GitHub-Login"

  result = Net::HTTP.start(uri.hostname, uri.port, use_ssl: true) do |http|
    http.request(request)
  end

  parse_response(result)
end

get "/" do
  <<~HTML
    <!doctype html>
    <html>
      <head><meta charset="utf-8"><title>Termux Claude Codex</title></head>
      <body>
        <h1>Termux Claude Codex</h1>
        <a href="/github/login">Login with GitHub</a>
      </body>
    </html>
  HTML
end

get "/github/login" do
  state = SecureRandom.urlsafe_base64(32)
  session[:oauth_state] = state

  redirect_uri = URI.encode_www_form_component(CALLBACK_URL)
  redirect "https://github.com/login/oauth/authorize?client_id=#{CLIENT_ID}&redirect_uri=#{redirect_uri}&state=#{state}"
end

get "/github/callback" do
  code = params["code"]
  returned_state = params["state"]

  halt 400, "Missing authorization code." unless code
  halt 403, "Invalid OAuth state." unless returned_state && returned_state == session.delete(:oauth_state)

  token_data = exchange_code(code)
  token = token_data["access_token"]

  halt 502, "GitHub did not return an access token." unless token

  user = user_info(token)
  handle = user["login"]
  name = user["name"] || handle

  # Keep the token server-side. Do not render or log the token.
  session[:github_user] = {
    "login" => handle,
    "name" => name,
    "id" => user["id"]
  }

  <<~HTML
    <!doctype html>
    <html>
      <head><meta charset="utf-8"><title>GitHub Login</title></head>
      <body>
        <h1>Successfully authorized</h1>
        <p>Welcome, #{Rack::Utils.escape_html(name)} (#{Rack::Utils.escape_html(handle)}).</p>
      </body>
    </html>
  HTML
end

get "/logout" do
  session.clear
  redirect "/"
end
