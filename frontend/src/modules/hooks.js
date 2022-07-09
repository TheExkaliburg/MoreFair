let hookCallingFunctions = {};

/**
 * Creates a new hook endpoint for users to register to.
 * @param {string} name - name of the hook
 * @param {(hook: Hook, ...args) => undefined} callFunc - function to call
 * @returns {(...args) => undefined} - function to call
 * @example
 * var beforeCreate = createHookEndpoint("beforeCreate", (hook, ...args) => {
 *  hook.callback({some: "data"}, args[3]);
 * });
 * //...
 * beforeCreate(); //calling all registered hooks
 */
export function createHookEndpoint(name, callFunc) {
  hookCallingFunctions[name] = function (...args) {
    let myHooks = window.hooks[name];
    if (!myHooks) return;
    myHooks.forEach((hook, index) => {
      //check if the hook is sorted correctly
      for (let i = index + 1; i < myHooks.length; i++) {
        if (myHooks[i].avoid.includes(hook.name)) {
          return;
        }
      }
      for (let i = 0; i < index; i++) {
        if (myHooks[i].needs.includes(hook.name)) {
          return;
        }
      }

      callFunc(hook, ...args);
      if (hook.once) {
        hook._unregister();
      }
    });
  };
  return hookCallingFunctions[name];
}

/**
 * Creates a new hook.
 * @param {string} event - event name to register to
 * @param {string} name - name of the hook (used to identify it by other hooks)
 * @param {string[]} needs - list of hooks that this hook needs to be called after (default: [])
 * @param {string[]} avoid - array of hooks that this hook should not be called after (default: [])
 * @param {boolean} once - if true, the hook will be unregistered after it is called once (default: false)
 */
export class Hook {
  constructor(event, name, callback, needs = [], avoid = [], once = false) {
    this.event = event;
    this.name = name;
    this.callback = callback;
    this.once = once;
    this.needs = needs;
    this.avoid = avoid;
    this._unregister = () => {
      window.hooks[this.event].splice(
        window.hooks[this.event].indexOf(this),
        1
      );
      sortHooks(this.event);
    };
  }
}

/**
 * Mostly used internally.
 * @param {string} event - event name to sort the hooks for
 */
function sortHooks(event) {
  console.log(`Sorting hooks for event ${event}`);
  const oldHooks = window.hooks[event].slice();
  //if a.avoid.includes(b) then b._needs.push(a)
  oldHooks.forEach((hook) => {
    hook._needs = hook.needs.slice();
    oldHooks.forEach((hookInner) => {
      if (hookInner.avoid.includes(hook.name)) {
        hook._needs.push(hookInner.name);
      }
    });
  });

  //make all _needs unique
  oldHooks.forEach((hook) => {
    hook._needs =
      hook._needs.filter((item, pos) => {
        return hook._needs.indexOf(item) == pos;
      }) || [];
  });

  //build a network of dependencies
  const network = {};
  oldHooks.forEach((hook) => {
    network[hook.name] = hook._needs;
  });

  //visit all nodes in the network and sort them
  const sorted = [];
  const visited = {};
  const visit = (node) => {
    if (visited[node]) return;
    visited[node] = true;
    if (network[node]) {
      network[node].forEach(visit);
      sorted.push(node);
    }
  };

  oldHooks.forEach((hook) => {
    if (!visited[hook.name]) {
      visit(hook.name);
    }
  });

  const sortedHooks = sorted.map((name) => {
    return oldHooks.find((hook) => hook.name == name);
  });

  //check if the hooks are sorted correctly
  let sortedCorrectly = true;
  sortedHooks.forEach((hook, index) => {
    for (let i = index + 1; i < sortedHooks.length; i++) {
      if (sortedHooks[i].avoid.includes(hook.name)) {
        console.log(
          `${"WARN: "} ${sortedHooks[i].name} should be before ${hook.name}`
        );
        sortedCorrectly = false;
        return;
      }
    }
    for (let i = 0; i < index; i++) {
      if (sortedHooks[i].needs.includes(hook.name)) {
        console.log(
          `${"WARN:"} ${sortedHooks[i].name} should be after ${hook.name}`
        );
        sortedCorrectly = false;
        return;
      }
    }
  });
  if (!sortedCorrectly) return false;

  //check if all the hooks are accounted for
  let allHooksAccountedFor = true;
  oldHooks.forEach((hook) => {
    if (!sortedHooks.includes(hook)) {
      console.log(`${"WARN:"} ${hook.name} is not accounted for`);
      allHooksAccountedFor = false;
    }
  });

  if (!allHooksAccountedFor) return false;

  //remove all hooks from the old list and add them to the new one
  window.hooks[event] = sortedHooks;

  return true;
}

/**
 * setsup the hooks system and exposes the parts of it that are needed
 */
export function hooksSystemSetup() {
  window.hooks = {};
  window.Hook = Hook;
  window.registerHook = function (hook) {
    if (!window.hooks[hook.event]) {
      window.hooks[hook.event] = [];
    }
    window.hooks[hook.event].push(hook);
    sortHooks(hook.event);
  };
}
