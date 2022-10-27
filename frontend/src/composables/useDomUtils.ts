function getMentionElement({ name, id }) {
  const mention = document.createElement("span");
  mention.innerHTML = `@${name}#${id}`;
  mention.classList.add("mention");
  mention.setAttribute("data-user", name);
  mention.setAttribute("data-id", id);
  mention.setAttribute("data-replaceChar", "@");
  return mention;
}

function getGroupMentionElement(groupName) {
  const mention = document.createElement("span");
  mention.innerHTML = `$${groupName}$`;
  mention.classList.add("mention");
  mention.setAttribute("data-group", groupName);
  mention.setAttribute("data-replaceChar", "$");
  return mention;
}

function getPlaintextElement(plainText) {
  return document.createTextNode(plainText);
}

/**
 *
 * @param {"START"|"END"|"START ELEMENT"|"END ELEMENT"} [caretEndPosition] - Where to put the caret after the element is inserted.
 * @param {"CARET" | "START" | "END"} [insertionPosition] - Where to insert the element.
 * @param {Number} [removeCharsAfterCaret] - Where to insert the element.
 */
function insertSpecialChatElement(
  element,
  insertionPosition,
  caretEndPosition,
  removeCharsAfterCaret = 0
) {
  const chatInput = document.getElementById("chatInput");
  // explicit hoisting of variables used in the switch statement
  let textNode;
  if (insertionPosition === "START") {
    if (chatInput.hasChildNodes()) {
      chatInput.insertBefore(element, chatInput.firstChild);
    } else {
      chatInput.appendChild(element);
    }
    // add a space after the element
    textNode = document.createTextNode(" ");
    chatInput.appendChild(textNode);
    chatInput.appendChild(document.createElement("br"));
  } else if (insertionPosition === "CARET") {
    // using var here because i dont want to hoist the variables myself
    const selection = document.getSelection();
    const text = selection.anchorNode.textContent;
    const caretPosition = document.getSelection().anchorOffset;
    const textBeforeCaret = text.substring(0, caretPosition);
    const textAfterCaret = text.substring(caretPosition);
    // Create the before and after text nodes fo surround the new element
    const before = document.createTextNode(
      textBeforeCaret.substring(
        0,
        textBeforeCaret.length - removeCharsAfterCaret
      )
    );
    const after = document.createTextNode(textAfterCaret);
    chatInput.replaceChild(after, selection.anchorNode);
    chatInput.insertBefore(element, after);
    chatInput.insertBefore(before, element);
    // add a space after the mention and put the cursor at the end of the text
    textNode = document.createTextNode(" ");
    chatInput.appendChild(textNode);
    chatInput.appendChild(document.createElement("br"));
  } else {
    chatInput.appendChild(element);
    // add a space after the element
    textNode = document.createTextNode(" ");
    chatInput.appendChild(textNode);
    chatInput.appendChild(document.createElement("br"));
  }
  chatInput.focus();
  // remove all br tags that have nextsibling
  const brs = chatInput.getElementsByTagName("br");
  for (let i = 0; i < brs.length; i++) {
    if (brs[i].nextSibling) {
      brs[i].remove();
    }
  }
  // hoisting of variables used in the switch statement
  const range = document.createRange();
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
    case "END":
    default:
      range.setStart(chatInput.lastChild, 0);
      break;
  }
  range.collapse(true);
  const sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(range);
  chatInput.focus();
}

export const useDomUtils = () => {
  return {
    getMentionElement,
    getGroupMentionElement,
    getPlaintextElement,
    insertSpecialChatElement,
  };
};
