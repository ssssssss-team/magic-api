class Store {
    constructor() {
    }

    set(key, value) {
        if (Array.isArray(value) || typeof value == 'object') {
            value = JSON.stringify(value);
        }
        localStorage.setItem(key, value);
    }

    remove(key) {
        localStorage.removeItem(key)
    }

    get(key) {
        return localStorage.getItem(key);
    }
}

export default new Store();