import {
  RemovableRef,
  StorageSerializers,
  useStorage,
  UseStorageOptions,
} from "@vueuse/core";
// @ts-ignore
import { deepMerge } from "@antfu/utils";
import { ObjectFunctions } from "~/store/entities/option";

export const useLocalStorage = (
  key: string,
  defaultValue: any,
  options?: UseStorageOptions<any> | undefined,
): RemovableRef<any> => {
  const result = useStorage(key, deepCopy(defaultValue), localStorage, {
    serializer: {
      write: options?.serializer?.write || write,
      read: options?.serializer?.read || read,
    },
    mergeDefaults: (storageValue: any, defaults: any) =>
      deepMerge(defaults, storageValue),
  });

  deleteOldValues(result.value, deepCopy(defaultValue));

  function read(value: any) {
    return deepMerge(
      deepCopy(defaultValue),
      StorageSerializers.object.read(value),
    );
  }

  function write(value: any) {
    const temp = deepCopy(value);
    deleteEntriesWithKey(temp, ["transient"]);
    return StorageSerializers.object.write(temp);
  }

  return result;
};

function deleteOldValues(state: any, defaults: any) {
  Object.keys(state).forEach((key) => {
    if (!(key in defaults) && !ObjectFunctions.includes(key)) {
      delete state[key];
    } else if (typeof state[key] === "object" && !Array.isArray(state[key])) {
      deleteOldValues(state[key], defaults[key]);
    }
  });
}

function deleteEntriesWithKey(state: any, keys: string[]) {
  Object.keys(state).forEach((key) => {
    if (keys.includes(key)) {
      delete state[key];
    } else if (typeof state[key] === "object" && !Array.isArray(state[key])) {
      deleteEntriesWithKey(state[key], keys);
    }
  });
}

function deepCopy<T>(source: T): T {
  return Array.isArray(source)
    ? source.map((item) => deepCopy(item))
    : source instanceof Date
    ? new Date(source.getTime())
    : source && typeof source === "object"
    ? Object.getOwnPropertyNames(source).reduce(
        (o, prop) => {
          Object.defineProperty(
            o,
            prop,
            Object.getOwnPropertyDescriptor(source, prop)!,
          );
          o[prop] = deepCopy((source as { [key: string]: any })[prop]);
          return o;
        },
        Object.create(Object.getPrototypeOf(source)),
      )
    : (source as T);
}
