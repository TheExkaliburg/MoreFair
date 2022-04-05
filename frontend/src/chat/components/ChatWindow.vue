<template>
  <div class="chat-window container rounded py-1 px-3">
    <div class="chat-header row py-1">
      <!--div class="col chat-info">Chad #{{ chat.currentChatNumber }}</div-->
      <PaginationGroup
        :current="chat.currentChatNumber"
        :max="user.highestCurrentLadder"
        :onChange="changeChat"
      />
    </div>
    <div ref="chatContent" class="chat-content row py-0">
      <ChatMessage
        v-for="message in chat.messages"
        :key="message"
        :msg="message"
      />
    </div>
    <div class="chat-input row py-3">
      <div class="input-group">
        <div
          id="chatInput"
          class="form-control shadow-none"
          contenteditable="true"
          placeholder="Chad is listening..."
          role="textbox"
          type="text"
          @keydown="chatBoxKeyDown"
        ></div>
        <button
          class="btn btn-outline-primary shadow-none"
          @click="sendMessage"
        >
          Send
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useStore } from "vuex";
import { computed, inject, onUpdated, ref } from "vue";
import { onMounted } from "vue";
import ChatMessage from "@/chat/components/ChatMessage";
import PaginationGroup from "@/components/PaginationGroup";

const store = useStore();
const stompClient = inject("$stompClient");

const message = ref("");
const chatContent = ref(null);

const chat = computed(() => store.state.chat.chat);
const user = computed(() => store.state.user);
const rankers = computed(() => store.getters["ladder/allRankers"]);

function sendMessage() {
  const [msg, metadata] = parseSendMessage();

  stompClient.send("/app/chat/post/" + chat.value.currentChatNumber, {
    content: msg,
    metadata: metadata,
  });
  message.value = "";
}

function changeChat(event) {
  const targetChat = event.target.dataset.chat;
  if (targetChat !== chat.value.currentChatNumber) {
    stompClient.unsubscribe("/topic/chat/" + chat.value.currentChatNumber);
    stompClient.subscribe("/topic/chat/" + targetChat, (message) => {
      store.commit({ type: "chat/addMessage", message: message });
    });
    stompClient.send("/app/chat/init/" + targetChat);
  }
}

function parseSendMessage() {
  const msgBox = document.getElementById("chatInput");
  const children = msgBox.childNodes;
  var msg = "";
  var mentions = [];
  for (let i = 0; i < children.length; i++) {
    if (children[i].nodeType == 3) {
      msg += children[i].nodeValue;
    } else if (children[i].classList.contains("mention")) {
      mentions.push({
        u: children[i].getAttribute("data-user"),
        id: children[i].getAttribute("data-id"),
        i: msg.length,
      });
      msg += `{@}`;
    }
  }
  //finally, replace all non-breaking spaces with regular spaces so vue doesn't choke on them
  msg = msg.replace(/\u00a0/g, " ");
  return [msg, mentions];
}

onMounted(() => {
  window.dropdownElementSelected = -1;
  const observer = new MutationObserver(function (mutations) {
    //on any mutation, check if the dropdown is open and if so, close it
    let oldDropdown = document.getElementById("mentionDropdown");
    if (oldDropdown) {
      oldDropdown.parentNode.removeChild(oldDropdown);
    }

    mutations.forEach(function (mutation) {
      if (mutation.type == "characterData") {
        onElementChange(mutation);
        return;
      }
      if (mutation.type == "childList") {
        //if the child list changed and we now have a text node and a br, we can also handle that
        if (
          mutation.addedNodes.length == 1 &&
          mutation.addedNodes[0].nodeType == 3
        ) {
          onElementChange({
            target: mutation.addedNodes[0],
            type: "characterData",
            oldValue: "",
          });
        }
      }
    });
  });
  let config = {
    attributes: true,
    childList: true,
    characterData: true,
    characterDataOldValue: true,
    subtree: true,
  };
  observer.observe(document.getElementById("chatInput"), config);
});

function mentionElementChanged(mutation) {
  let parent = mutation.target.parentElement;

  var oldText = `@${parent.getAttribute("data-user")}#${parent.getAttribute(
    "data-id"
  )}`;
  var newText = mutation.target.parentElement.innerText;

  //Handling deletions
  if (oldText.length > newText.length) {
    parent.parentNode.removeChild(parent);
  }

  //Handling insertions
  if (oldText.length < newText.length) {
    var diffText = "";
    var diffOffset = 0;
    var firstDif = newText.length + 1; //keeping track of the first diff to determine if we inserted at the start or not
    for (let i = 0; i < newText.length; i++) {
      if (newText[i + diffOffset] != oldText[i]) {
        if (firstDif > i) {
          firstDif = i;
        }
        diffText += newText[i];
        diffOffset++;
      }
    }

    //resetting text no matter what
    parent.innerText = oldText;

    //checking if we have a diff
    if (diffText.length <= 0) {
      return; //no diff, nothing to do
    }

    //add the new text to the next or prev sibling of the mention
    if (firstDif == 0) {
      //add to previous sibling
      var prevSibling = parent.previousSibling;
      if (prevSibling && prevSibling.tagName == "#text") {
        prevSibling.textContent += diffText;
        return;
      }
      var newTextNode = document.createTextNode(diffText);
      parent.parentNode.insertBefore(newTextNode, parent);
      return;
    }
    //add to next sibling
    var nextSibling = parent.nextSibling;
    if (nextSibling && nextSibling.tagName == "#text") {
      nextSibling.textContent = diffText + nextSibling.textContent;
    } else {
      newTextNode = document.createTextNode(diffText);
      parent.parentNode.insertBefore(newTextNode, parent.nextSibling);
    }
    //set document cursor to the end of the diff text
    var range = document.createRange();
    range.setStart(newTextNode, diffText.length);
    range.collapse(true);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
  }
}

function getMentionElement({ name, id }) {
  let mention = document.createElement("span");
  mention.innerHTML = `@${name}#${id}`;
  mention.style.backgroundColor = "rgb(70, 70, 70)";
  mention.style.selectionColor = "rgb(70, 70, 70)";
  mention.style.padding = "2px";
  mention.style.border = "1px solid black";
  mention.style.borderRadius = "5px";
  mention.style.cursor = "pointer";
  mention.style.fontWeight = "bold";
  mention.classList.add("mention");
  mention.setAttribute("data-user", name);
  mention.setAttribute("data-id", id);

  return mention;
}

function plainTextElementChanged(mutation) {
  let textElement = mutation.target;
  let text = textElement.textContent;

  //If we have no text, all is good.
  if (text.length == 0) {
    return;
  }

  //now we are getting the caret position and the text before and after the caret
  let caretPosition = document.getSelection().anchorOffset;
  let textBeforeCaret = text.substring(0, caretPosition);
  let textAfterCaret = text.substring(caretPosition);

  //Now that we know where the caret is, we can check if we have a mention in the text before the caret
  let firstMetion = textBeforeCaret.indexOf("@");
  let possibleMentionLength = 0;
  let possibleMentions = [];
  while (firstMetion > -1) {
    textBeforeCaret = textBeforeCaret.substring(firstMetion + 1);
    firstMetion = textBeforeCaret.indexOf("@");

    //Checking if any rankerName#id is in the text
    var possibleMentionLower = textBeforeCaret.toLowerCase();
    possibleMentions = [];
    for (let i = 0; i < rankers.value.length; i++) {
      if (
        (rankers.value[i].username + "#" + rankers.value[i].accountId)
          .toLowerCase()
          .startsWith(possibleMentionLower)
      ) {
        possibleMentions.push({
          name: rankers.value[i].username,
          id: rankers.value[i].accountId,
        });
      }
    }
    possibleMentionLength = possibleMentionLower.length + 1;
    //If we found any, we can exit this loop
    if (possibleMentions.length > 0) {
      break;
    }
  }
  window.possibleMention = possibleMentions;
  if (possibleMentions.length == 0) {
    return; //no possible mentions
  }
  //We have possible mentions, so now we should display our dropdown

  //First, sorting the mentions by their name and accountId
  possibleMentions.sort((a, b) => {
    if (a.name < b.name) {
      return 1;
    }
    if (a.name > b.name) {
      return -1;
    }
    if (a.id < b.id) {
      return 1;
    }
    if (a.id > b.id) {
      return -1;
    }
    return 0;
  });

  //Now we can create the dropdown
  let dropdown = document.createElement("div");
  dropdown.id = "mentionDropdown";
  dropdown.style.display = "block";
  dropdown.innerHTML = "";
  for (let i = 0; i < possibleMentions.length; i++) {
    var option = document.createElement("option");
    option.innerHTML = `${possibleMentions[i].name}#${possibleMentions[i].id}`;

    option.style.border = "1px solid black";

    option.style.paddingRight = "5px";
    option.style.paddingLeft = "5px";

    option.addEventListener("click", function () {
      let msgBox = document.getElementById("chatInput");
      let mention = getMentionElement(possibleMentions[i]);

      //split the text node into two
      textBeforeCaret = text.substring(0, caretPosition);
      textAfterCaret = text.substring(caretPosition);
      let before = document.createTextNode(
        textBeforeCaret.substring(
          0,
          textBeforeCaret.length - possibleMentionLength
        )
      );
      let after = document.createTextNode(textAfterCaret);

      msgBox.replaceChild(after, textElement);
      msgBox.insertBefore(mention, after);
      msgBox.insertBefore(before, mention);

      //add a space after the mention and put the cursor at the end of the text
      var textNode = document.createTextNode(" ");
      msgBox.appendChild(textNode);
      msgBox.appendChild(document.createElement("br"));
      msgBox.focus();

      //remove the dropdown
      dropdown.remove();

      //remove all br tags that have nextsibling
      let brs = msgBox.getElementsByTagName("br");
      for (let i = 0; i < brs.length; i++) {
        if (brs[i].nextSibling) {
          brs[i].remove();
        }
      }

      var range = document.createRange();
      range.setStart(textNode, 1);
      range.collapse(true);
      var sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(range);
      msgBox.focus();
    });

    dropdown.appendChild(option);
  }
  //add the dropdown to the document
  var navbar = document.getElementById("chatInput");
  document.body.appendChild(dropdown);

  var offLeft = 0;
  var offBot = 0;

  var bounds = navbar.getBoundingClientRect();
  offLeft = bounds.left;
  offBot = visualViewport.height - bounds.y + 5;

  dropdown.style.bottom = offBot + "px";
  dropdown.style.left = offLeft + "px";
  dropdown.style.position = "absolute";
  dropdown.style.background = "#10141f";
  dropdown.style.border = "1px solid #de9e41";
  dropdown.style.borderRadius = "5px";
  dropdown.style.zIndex = "1000";
  dropdown.style.padding = "5px";

  //limit the dropdown height to 300px
  if (dropdown.offsetHeight > 300) {
    dropdown.style.height = "300px";
    dropdown.style.overflowY = "scroll";
    //scroll to the bottom
    dropdown.scrollTop = dropdown.scrollHeight;
  }

  //select the last element
  window.dropdownElementSelected = possibleMentions.length - 1;
  dropdown.children[window.dropdownElementSelected].style.backgroundColor =
    "rgb(70, 70, 70)";
}

function onElementChange(mutation) {
  let parent = mutation.target.parentElement;
  if (parent?.classList.contains("mention")) {
    mentionElementChanged(mutation);
    return;
  }
  plainTextElementChanged(mutation);
}

function chatBoxKeyDown(e) {
  var textNodes = document.getElementById("chatInput").childNodes;
  for (let i = 0; i < textNodes.length; i++) {
    if (textNodes[i].nodeType == 3) {
      if (i + 1 < textNodes.length && textNodes[i + 1].nodeType == 3) {
        textNodes[i].textContent += textNodes[i + 1].textContent;
        textNodes[i + 1].remove();
      }
    }
  }

  //if the key is key up or down or tab
  if (e.keyCode == 38 || e.keyCode == 40 || e.keyCode == 9) {
    e.preventDefault();
    if (!document.getElementById("mentionDropdown")) {
      return;
    }

    //unhighlight all the options
    for (
      let i = 0;
      i < document.getElementById("mentionDropdown").children.length;
      i++
    ) {
      document.getElementById("mentionDropdown").children[
        i
      ].style.backgroundColor = "";
    }

    //increment or decrement dropdownElementSelected
    if (e.keyCode === 38) {
      window.dropdownElementSelected--;
    } else if (e.keyCode == 40) {
      window.dropdownElementSelected++;
    }

    //if the selected element is out of bounds
    if (window.dropdownElementSelected < 0) {
      window.dropdownElementSelected =
        document.getElementById("mentionDropdown").children.length - 1;
    } else if (
      window.dropdownElementSelected >=
      document.getElementById("mentionDropdown").children.length
    ) {
      window.dropdownElementSelected = 0;
    }

    if (window.dropdownElementSelected < 0) {
      return;
    }

    //scroll the selected element into view using the scrollTop property
    if (window.dropdownElementSelected > -1) {
      //if the selected element is not fully visible
      if (
        document.getElementById("mentionDropdown").children[
          window.dropdownElementSelected
        ].offsetTop +
          document.getElementById("mentionDropdown").children[
            window.dropdownElementSelected
          ].offsetHeight >
        document.getElementById("mentionDropdown").scrollTop +
          document.getElementById("mentionDropdown").offsetHeight
      ) {
        document.getElementById("mentionDropdown").scrollTop =
          document.getElementById("mentionDropdown").children[
            window.dropdownElementSelected
          ].offsetTop +
          document.getElementById("mentionDropdown").children[
            window.dropdownElementSelected
          ].offsetHeight -
          document.getElementById("mentionDropdown").offsetHeight;
      } else if (
        document.getElementById("mentionDropdown").children[
          window.dropdownElementSelected
        ].offsetTop < document.getElementById("mentionDropdown").scrollTop
      ) {
        document.getElementById("mentionDropdown").scrollTop =
          document.getElementById("mentionDropdown").children[
            window.dropdownElementSelected
          ].offsetTop;
      }
    }

    //highlight the selected element
    document.getElementById("mentionDropdown").children[
      window.dropdownElementSelected
    ].style.backgroundColor = "rgb(70, 70, 70)";
  }

  //if the key is enter or tab
  if (e.keyCode == 13 || e.keyCode == 9) {
    //if the mentionDropdown is open and we have a selection
    if (
      document.getElementById("mentionDropdown") &&
      window.dropdownElementSelected != -1
    ) {
      //click the selected element
      e.preventDefault();
      document
        .getElementById("mentionDropdown")
        .children[window.dropdownElementSelected].click();
      return;
    } else {
      e.preventDefault();
      sendMessage();
      document.getElementById("chatInput").innerText = "";
    }
  }
}

onUpdated(() => {
  chatContent.value.scrollTop = chatContent.value.scrollHeight;
});
</script>

<style lang="scss" scoped>
@import "../../styles/styles";
// .
.chat-window {
  height: 100%;
}

.chat-pagination {
  white-space: nowrap;
  text-align: end;
  padding-right: 0px;
}

.chat-content {
  overflow-y: auto;
  overflow-x: hidden;
  align-content: start;
  height: calc(100% - calc(70px + 40px));
}

.chat-info {
  white-space: nowrap;
  overflow-x: hidden;
}

#chatInput {
  background-color: #10141f;
  color: #de9e41;
}

.chat-header {
  max-height: 48px;
}
</style>
