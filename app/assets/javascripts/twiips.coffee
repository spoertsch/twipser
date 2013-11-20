
#wsd
ws = new WebSocket("ws://localhost:9000/feed")

#wsom
ws.onmessage = (event) ->
  twiip = JSON.parse(event.data)
  markup = $("<blockquote>").append($("<p>").text(twiip.message))
    .append($("<small>").text(twiip.author + " (" + twiip.createdAt + ")"))
  $("#all-twiips").prepend(markup)
  

console.log(formatDate(new Date()))
###
#wsf
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
)
###
