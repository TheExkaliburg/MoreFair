<template>
  <div class="input-group relative">
    <div class="chatInputPlaceholder">Chad is listening...</div>
    <div
      id="chatInput"
      class="form-control shadow-none"
      contenteditable="true"
      role="textbox"
      spellcheck="false"
      type="text"
      @keydown="chatBoxKeyDown"
    ></div>
    <button class="btn btn-outline-primary shadow-none" @click="sendMessage">
      Send
    </button>
  </div>
  <div :class="msgLength > 140 ? 'error' : ''" class="msgLength">
    Message length: {{ msgLength }} / 140
  </div>
</template>

<script>
/**
 *
 * @param {"START"|"END"|"START ELEMENT"|"END ELEMENT"} [caretEndPosition] - Where to put the caret after the element is inserted.
 * @param {"CARET" | "START" | "END"} [insertionPosition] - Where to insert the element.
 * @param {Number} [removeCharsAfterCaret] - Where to insert the element.
 */
export function insertSpecialChatElement(
  element,
  insertionPosition,
  caretEndPosition,
  removeCharsAfterCaret = 0
) {
  const chatInput = document.getElementById("chatInput");

  //explicit hoisting of variables used in the switch statement
  let textNode, brs;

  switch (insertionPosition) {
    case "START":
      if (chatInput.hasChildNodes()) {
        chatInput.insertBefore(element, chatInput.firstChild);
      } else {
        chatInput.appendChild(element);
      }
      //add a space after the element
      textNode = document.createTextNode(" ");
      chatInput.appendChild(textNode);
      chatInput.appendChild(document.createElement("br"));
      break;

    case "CARET":
      //using var here because i dont want to hoist the variables myself
      var selection = document.getSelection();
      var text = selection.anchorNode.textContent;

      var caretPosition = document.getSelection().anchorOffset;
      var textBeforeCaret = text.substring(0, caretPosition);
      var textAfterCaret = text.substring(caretPosition);

      //Create the before and after text nodes fo surround the new element
      var before = document.createTextNode(
        textBeforeCaret.substring(
          0,
          textBeforeCaret.length - removeCharsAfterCaret
        )
      );
      var after = document.createTextNode(textAfterCaret);

      chatInput.replaceChild(after, selection.anchorNode);
      chatInput.insertBefore(element, after);
      chatInput.insertBefore(before, element);

      //add a space after the mention and put the cursor at the end of the text
      textNode = document.createTextNode(" ");
      chatInput.appendChild(textNode);
      chatInput.appendChild(document.createElement("br"));
      break;

    default:
    case "END":
      chatInput.appendChild(element);
      //add a space after the element
      textNode = document.createTextNode(" ");
      chatInput.appendChild(textNode);
      chatInput.appendChild(document.createElement("br"));

      break;
  }
  chatInput.focus();
  //remove all br tags that have nextsibling
  brs = chatInput.getElementsByTagName("br");
  for (let i = 0; i < brs.length; i++) {
    if (brs[i].nextSibling) {
      brs[i].remove();
    }
  }

  //hoisting of variables used in the switch statement
  let range = document.createRange();

  switch (caretEndPosition) {
    case "START":
      range.setStart(chatInput.firstChild, 0);
      break;
    case "START ELEMENT":
      range.setStartBefore(element);
      break;
    case "END ELEMENT":
      range.setStartAfter(textNode);
      break;
    default:
    case "END":
      range.setStart(chatInput.lastChild, 0);
      break;
  }
  range.collapse(true);
  let sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(range);
  chatInput.focus();
}
</script>

<script setup>
import { ref, onMounted, computed, inject } from "vue";
import { useStore } from "vuex";

const msgLength = ref(0);

const store = useStore();
const chat = computed(() => store.state.chat.chat);
const rankers = computed(() => store.getters["ladder/allRankers"]);
const stompClient = inject("$stompClient");

onMounted(() => {
  document.getElementById("mentionDropdown").style.display = "none";
  window.dropdownElementSelected = -1;
  const observer = new MutationObserver(function (mutations) {
    //If there is text in the box, we need to hide the placeholder, if not we need to show it.
    if (document.getElementById("chatInput").textContent.trim() == "") {
      document.getElementsByClassName("chatInputPlaceholder")[0].style.display =
        "flex";
    } else {
      document.getElementsByClassName("chatInputPlaceholder")[0].style.display =
        "none";
    }

    mutations.forEach(function (mutation) {
      if (mutation.type === "characterData") {
        onElementChange(mutation);
        return;
      }
      if (mutation.type === "childList") {
        //if the child list changed and we now have a text node and a br, we can also handle that
        if (
          mutation.addedNodes.length === 1 &&
          mutation.addedNodes[0].nodeType === 3
        ) {
          onElementChange({
            target: mutation.addedNodes[0],
            type: "characterData",
            oldValue: "",
          });
        } else {
          if (mutation.addedNodes.length > 0) {
            //validate any potential added spans
            mutation.addedNodes.forEach(function (node) {
              if (node.tagName === "BR") {
                return;
              }
              if (node.tagName === "SPAN") {
                if (node.classList.contains("mention")) {
                  onElementChange({
                    target: node,
                    type: "characterData",
                    oldValue: "",
                  });
                }
              } else {
                //convert it to a text node
                const textNode = document.createTextNode(node.innerText);
                node.parentNode.replaceChild(textNode, node);
              }
            });
          }
          //If the chat box was cleared, we need to remove the mention elements
          if (document.getElementById("chatInput").innerText.trim() === "") {
            let oldDropdown = document.getElementById("mentionDropdown");
            if (oldDropdown) {
              oldDropdown.style.display = "none";
            }
          }
        }
      }
    });
    //Remove all nodes that are not text nodes, spans, or brs
    const msgBox = document.getElementById("chatInput");
    const children = msgBox.childNodes;
    for (let i = 0; i < children.length; i++) {
      if (
        children[i].nodeType !== 3 &&
        children[i].tagName !== "SPAN" &&
        children[i].tagName !== "BR"
      ) {
        msgBox.removeChild(children[i]);
      }
    }

    //finally, update the message length counter
    msgLength.value = parseSendMessage()[0].length;
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
function onElementChange(mutation) {
  //on any mutation, check if the dropdown is open and if so, close it
  let oldDropdown = document.getElementById("mentionDropdown");
  if (oldDropdown) {
    oldDropdown.style.display = "none";
  }

  let parent = mutation.target.parentElement;
  if (parent?.classList.contains("mention")) {
    mentionElementChanged(mutation);
    return;
  }

  plainTextElementChanged(mutation);
}
function plainTextElementChanged(mutation) {
  let textElement = mutation.target;
  let text = textElement.textContent;

  //If we have no text, all is good.
  if (text.length === 0) {
    return;
  }
  if (!textElement.parentNode) {
    return; //Unconnected node
  }

  //now we are getting the caret position and the text before and after the caret
  let caretPosition = document.getSelection().anchorOffset;
  let textBeforeCaret = text.substring(0, caretPosition);

  //Here, we find out where to put the popup later so it follows the caret.
  let dummySpan = document.createElement("span");
  dummySpan.innerText = textBeforeCaret;
  textElement.parentNode.insertBefore(dummySpan, textElement);
  let caretX = dummySpan.getBoundingClientRect().right;
  dummySpan.parentNode.removeChild(dummySpan);

  //Now that we know where the caret is, we can check if we have a mention in the text before the caret
  let firstMetion = textBeforeCaret.indexOf("@");
  let possibleMentionLength = 0;
  let possibleMentions = [];
  while (firstMetion > -1) {
    textBeforeCaret = textBeforeCaret.substring(firstMetion + 1);
    firstMetion = textBeforeCaret.indexOf("@");

    //Checking if any rankerName#id is in the text
    let possibleMentionLower = textBeforeCaret.toLowerCase();
    possibleMentions = [];
    for (let i = 0; i < rankers.value.length; i++) {
      if (
        (rankers.value[i].username + "#" + rankers.value[i].accountId)
          .toLowerCase()
          .startsWith(possibleMentionLower) ||
        ("#" + rankers.value[i].accountId)
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
  if (possibleMentions.length === 0) {
    return; //no possible mentions
  }
  //We have possible mentions, so now we should display our dropdown

  let numberMention = textBeforeCaret.toLowerCase().startsWith("#");

  //First, sorting the mentions by their name and accountId
  possibleMentions.sort((a, b) => {
    if (numberMention) {
      //If we are looking for a number, we want a different order to put the numbers lower than the names (and select them easier)
      if (("#" + a.id).startsWith(textBeforeCaret.toLowerCase())) {
        if (("#" + b.id).startsWith(textBeforeCaret.toLowerCase())) {
          return a.id - b.id;
        }
        return 1;
      }
      if (("#" + b.id).startsWith(textBeforeCaret.toLowerCase())) {
        return -1;
      }
    }
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
  let dropdown = document.getElementById("mentionDropdown");
  dropdown.style.display = "block";
  dropdown.innerHTML = "";
  for (let i = 0; i < possibleMentions.length; i++) {
    let option = document.createElement("option");
    option.innerHTML = `${possibleMentions[i].name}#${possibleMentions[i].id}`;

    option.style.border = "1px solid black";

    option.style.paddingRight = "5px";
    option.style.paddingLeft = "5px";

    option.addEventListener("click", function () {
      let mention = getMentionElement(possibleMentions[i]);
      //We need to insert the mention into the text
      insertSpecialChatElement(
        mention,
        "CARET",
        "END ELEMENT",
        possibleMentionLength
      );
      //We need to close the dropdown
      dropdown.style.display = "none";
    });

    dropdown.appendChild(option);
  }

  let navbar = document.getElementById("chatInput");
  let offBot;

  let bounds = navbar.getBoundingClientRect();
  offBot = visualViewport.height - bounds.y + 5;

  dropdown.style.bottom = offBot + "px";
  dropdown.style.height = "auto";
  //limit the dropdown height to 300px
  if (dropdown.offsetHeight > 300) {
    dropdown.style.height = "300px";
    //scroll to the bottom
    dropdown.scrollTop = dropdown.scrollHeight;
  }

  //select the last element
  window.dropdownElementSelected = possibleMentions.length - 1;
  dropdown.children[window.dropdownElementSelected].style.backgroundColor =
    "var(--item-selected-color)";
  //make sure the dropdown always stays 5 pixels from the right window edge
  caretX = Math.min(caretX, visualViewport.width - 5 - dropdown.offsetWidth);
  dropdown.style.left = caretX + "px";
}

function mentionElementChanged(mutation) {
  let parent = mutation.target.parentElement;

  let oldText = `@${parent.getAttribute("data-user")}#${parent.getAttribute(
    "data-id"
  )}`;
  let newText = mutation.target.parentElement.innerText;

  //Handling deletions
  if (oldText.length > newText.length) {
    parent.parentNode.removeChild(parent);
  }

  //Handling insertions
  if (oldText.length < newText.length) {
    let diffText = "";
    let diffOffset = 0;
    let firstDif = newText.length + 1; //keeping track of the first diff to determine if we inserted at the start or not
    for (let i = 0; i < newText.length; i++) {
      if (newText[i + diffOffset] !== oldText[i]) {
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

    let newTextNode;
    //add the new text to the next or prev sibling of the mention
    if (firstDif === 0) {
      //add to previous sibling
      let prevSibling = parent.previousSibling;
      if (prevSibling && prevSibling.tagName === "#text") {
        prevSibling.textContent += diffText;
        return;
      }
      newTextNode = document.createTextNode(diffText);
      parent.parentNode.insertBefore(newTextNode, parent);
      return;
    }
    //add to next sibling
    let nextSibling = parent.nextSibling;
    if (nextSibling && nextSibling.tagName === "#text") {
      nextSibling.textContent = diffText + nextSibling.textContent;
    } else {
      newTextNode = document.createTextNode(diffText);
      parent.parentNode.insertBefore(newTextNode, parent.nextSibling);
    }
    //set document cursor to the end of the diff text
    const range = document.createRange();
    range.setStart(newTextNode, diffText.length);
    range.collapse(true);
    const sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
  }
}

function getMentionElement({ name, id }) {
  let mention = document.createElement("span");
  mention.innerHTML = `@${name}#${id}`;
  mention.classList.add("mention");
  mention.setAttribute("data-user", name);
  mention.setAttribute("data-id", id);

  return mention;
}

function parseSendMessage() {
  const msgBox = document.getElementById("chatInput");
  const children = msgBox.childNodes;
  let msg = "";
  let mentions = [];
  for (let i = 0; i < children.length; i++) {
    if (children[i].nodeType === 3) {
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

function chatBoxKeyDown(e) {
  let textNodes = document.getElementById("chatInput").childNodes;
  for (let i = 0; i < textNodes.length; i++) {
    if (textNodes[i].nodeType === 3) {
      if (i + 1 < textNodes.length && textNodes[i + 1].nodeType === 3) {
        textNodes[i].textContent += textNodes[i + 1].textContent;
        textNodes[i + 1].remove();
      }
    }
  }

  const mentionDropdown = document.getElementById("mentionDropdown");

  //if the key is key up or down or tab
  if (e.keyCode === 38 || e.keyCode === 40 || e.keyCode === 9) {
    e.preventDefault();
    if (!mentionDropdown || mentionDropdown.style.display == "none") {
      return;
    }

    //unhighlight all the options
    for (let i = 0; i < mentionDropdown.children.length; i++) {
      mentionDropdown.children[i].style.backgroundColor = "";
    }

    //increment or decrement dropdownElementSelected
    if (e.keyCode === 38) {
      window.dropdownElementSelected--;
    } else if (e.keyCode === 40) {
      window.dropdownElementSelected++;
    }

    //if the selected element is out of bounds
    if (window.dropdownElementSelected < 0) {
      window.dropdownElementSelected = mentionDropdown.children.length - 1;
    } else if (
      window.dropdownElementSelected >= mentionDropdown.children.length
    ) {
      window.dropdownElementSelected = 0;
    }

    if (window.dropdownElementSelected < 0) {
      return;
    }
    const selectedElement =
      mentionDropdown.children[window.dropdownElementSelected];
    //scroll the selected element into view using the scrollTop property
    if (window.dropdownElementSelected > -1) {
      //if the selected element is not fully visible
      if (
        selectedElement.offsetTop + selectedElement.offsetHeight >
        mentionDropdown.scrollTop + mentionDropdown.offsetHeight
      ) {
        mentionDropdown.scrollTop =
          selectedElement.offsetTop +
          selectedElement.offsetHeight -
          mentionDropdown.offsetHeight;
      } else if (selectedElement.offsetTop < mentionDropdown.scrollTop) {
        mentionDropdown.scrollTop = selectedElement.offsetTop;
      }
    }

    //highlight the selected element
    selectedElement.style.backgroundColor = "var(--item-selected-color)";
  }

  //if the key is enter or tab
  if (e.keyCode === 13 || e.keyCode === 9) {
    //if the mentionDropdown is open and we have a selection
    if (
      mentionDropdown?.style.display != "none" &&
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

function sendMessage() {
  const [msg, metadata] = parseSendMessage();

  stompClient.send("/app/chat/post/" + chat.value.currentChatNumber, {
    content: msg,
    metadata: metadata,
  });
  message.value = "";
  //Sometime the chat box is not cleared, so we do it manually
  document.getElementById("chatInput").innerHTML = "";
}

const message = ref("");
</script>

<style lang="scss" scoped>
.relative {
  position: relative;
}

#chatInput {
  background-color: rgba(0, 0, 0, 0);
  color: var(--main-color);
  overflow-x: scroll;
  white-space: nowrap;
  //hide scrollbar
  -ms-overflow-style: none;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }

  border: 1px solid var(--button-color);
  border-top-left-radius: 5px;
  border-bottom-left-radius: 5px;
}

.chatInputPlaceholder {
  z-index: 3;
  color: var(--placeholder-color) !important;
  font-size: 1.2em;
  //font-weight: bold;
  white-space: nowrap;
  overflow-x: hidden;
  text-overflow: ellipsis;
  width: fit-content;
  height: 100%;
  position: absolute;

  margin-left: 0.6em;

  display: flex;
  justify-content: center;
  align-content: center;
  flex-direction: column;

  pointer-events: none;
}

.error {
  color: var(--text-error-color);
}
</style>
