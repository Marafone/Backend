const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/game'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function connect() {
    stompClient.activate();
    console.log("Connected");
}

function disconnect() {
    stompClient.deactivate();
    console.log("Disconnected");
}

function subscribeToGame(){
    console.log("Started listening for events in game " + $( "#gameId" ).val());
    stompClient.subscribe('/topic/game/' + $( "#gameId" ).val(), (event) => {
        showEvent(JSON.parse(event.body));
    });
}

async function createGame() {
  const url = "http://localhost:8080/game/create";
  try {
    const response = await fetch(url,{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            gameType: "MARAFFA",
            joinGameCode: null
        })
    });
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }
    const json = await response.text();
    console.log(json);
  } catch (error) {
    console.error(error.message);
  }
}

async function joinGame(){
  const url = "http://localhost:8080/game/" + $( "#gameId" ).val() + "/join";
  try {
    const response = await fetch(url,{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            team: "RED",
            joinGameCode: null
        })
    });
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }

    //const json = await response.json(); there will be no response
    console.log(response.status);
  } catch (error) {
    console.error(error.message);
  }
}

function sendCard() {
    const id = Math.random() < 0.5 ? 1 : 2; // Randomly choose between 1 and 2 (to be sure that only people in lobby 1 get message)
    console.log("Sending card to game with id: " + id);
    stompClient.publish({
        destination: `/app/game/${id}/card`,
        body: JSON.stringify({'card': "1"})
    });
}

function sendSuit() {
    const id = Math.random() < 0.5 ? 1 : 2; // Randomly choose between 1 and 2
    console.log("Sending suit to game with id: " + id);
    stompClient.publish({
        destination: `/app/game/${id}/suit`,
        body: JSON.stringify({'suit': "2"})
    });
}

function showEvent(message) {
    $("#events").append("<tr><td>" + JSON.stringify(message) + "</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#createGame" ).click(() => createGame());
    $( "#subscribeGame" ).click(() => subscribeToGame());
    $( "#joinGame" ).click(() => joinGame());
    $( "#sendCard" ).click(() => sendCard());
    $( "#sendSuit" ).click(() => sendSuit());
});

