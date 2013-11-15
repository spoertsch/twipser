ws = new WebSocket("ws://localhost:9000/feed")

ws.onmessage = (event) ->
  message = JSON.parse(event.data)
  markup = $("<blockquote>").append($("<p>").text(message.message))
    .append($("<small>").text(message.author))
  $("#all-messages").prepend(markup)


###
$(window).ready(() ->
  $("form").submit((event) ->
    ws.send(JSON.stringify(
      author: $("#author").val()
      message: $("#message").val()
    ))
    $("#author").val("")
    $("#message").val("")
    false
  )
)###