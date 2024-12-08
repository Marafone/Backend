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
    stompClient.subscribe('/user/queue/game', (event) => {
        console.log("Received private event.")
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

async function joinGame(team){
  const url = "http://localhost:8080/game/" + $( "#gameId" ).val() + "/join";
  try {
    const response = await fetch(url,{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            team: team,
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

async function logIn(){
  const url = "http://localhost:8080/auth/login";
  try {
    const response = await fetch(url,{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username:  $( "#username" ).val(),
            password:  $( "#password" ).val()
        })
    });
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }

    console.log(response.status);
    console.log("Logged in as user")
  } catch (error) {
    console.error(error.message);
  }
}

async function register(){
  const url = "http://localhost:8080/auth/register";
  try {
    const response = await fetch(url,{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username:  $( "#username" ).val(),
            email: "user@gmail.com",
            password:  $( "#password" ).val()
        })
    });
    if (!response.ok) {
      throw new Error(`Response status: ${response.status}`);
    }

    console.log(response.status);
    console.log("Registered as user")
  } catch (error) {
    console.error(error.message);
  }
}

function startGame(){
    console.log("Starting game with id: " + $("#gameId").val());
    console.log(`/app/game/${$("#gameId").val()}/start`);
    stompClient.publish({
        destination: `/app/game/${$("#gameId").val()}/start`
    });
}

function sendCard() {
    console.log("Sending card to game with id: " + $("#gameId").val());
    stompClient.publish({
        destination: `/app/game/${$("#gameId").val()}/card`,
        body: JSON.stringify({'cardId': $( "#cardId" ).val()})
    });
}

function sendSuit() {
    const selectSuit = Math.random() < 0.5 ? "CLUBS" : "SWORDS"; // Randomly choose
    stompClient.publish({
        destination: `/app/game/${$("#gameId").val()}/suit`,
        body: JSON.stringify({'trumpSuit': selectSuit})
    });
}

function reconnect() {
    stompClient.publish({
        destination: `/app/game/${$("#gameId").val()}/reconnect`
    });
}

function save() {
    stompClient.publish({
        destination: `/app/game/${$("#gameId").val()}/save`
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
    $( "#joinGameRed" ).click(() => joinGame("RED"));
    $( "#joinGameBlue" ).click(() => joinGame("BLUE"));
    $( "#startGame" ).click(() => startGame());
    $( "#sendCard" ).click(() => sendCard());
    $( "#sendSuit" ).click(() => sendSuit());
    $( "#login" ).click(() => logIn());
    $( "#register" ).click(() => register());
    $( "#reconnect" ).click(() => reconnect());
    $( "#save" ).click(() => save());
});

