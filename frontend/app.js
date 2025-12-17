// app.js
function sendMessage() {
    const from = document.getElementById("currentUser").value;
    const to = document.getElementById("toUser").value;
    const text = document.getElementById("message").value;

    if (!text.trim() || from === to) return;
   
    fetch("http://localhost:8080/send", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&text=${encodeURIComponent(text)}`
    });

    document.getElementById("message").value = "";
}

function loadMessages() {
    const currentUser = document.getElementById("currentUser").value;
    const chatBox = document.getElementById("chatBox");

    fetch("http://localhost:8080/messages")
        .then(res => res.text())
        .then(data => {
            chatBox.innerHTML = "";

            const lines = data.split("\n");

            lines.forEach(line => {
                if (!line.includes("→")) return;

                const [fromPart, rest] = line.split(" → ");
                const [toPart, message] = rest.split(" : ");

                if (fromPart !== currentUser && toPart !== currentUser) return;

                const msgDiv = document.createElement("div");

                if (fromPart === currentUser) {
                    msgDiv.className = "msg sent";
                    msgDiv.textContent = message;
                } else {
                    msgDiv.className = "msg received";
                    msgDiv.textContent = message;
                }

                chatBox.appendChild(msgDiv);
            });

            chatBox.scrollTop = chatBox.scrollHeight;
        });
}

function updateToUserOptions() {
    const currentUser = document.getElementById("currentUser").value;
    const toUserSelect = document.getElementById("toUser");

    for (let option of toUserSelect.options) {
        option.disabled = (option.value === currentUser);
    }
}

setInterval(loadMessages, 1000);
updateToUserOptions();
