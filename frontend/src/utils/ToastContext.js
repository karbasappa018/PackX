import { createContext } from "react";

const ToastContext = createContext({
    pushToast: () => {},
});

export default ToastContext;
