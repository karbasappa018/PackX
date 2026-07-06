import { lazy, Suspense, useState } from "react";
import React from "react";
import ReactDOM from "react-dom/client";
import Header from "./Components/Header.js";
import Dashboard from "./Components/Dashboard";
import { createBrowserRouter, Outlet, RouterProvider } from "react-router-dom";
import Pack from "./Components/Pack.js";
import Unpack from "./Components/Unpack.js";
import History from "./Components/History.js";
import OperationDetails from "./Components/OperationDetails.js";
import Error from "./Components/Error.js";
import ToastStack from "./Components/ToastStack.js";
import ToastContext from "./utils/ToastContext";

/*
    Chunking
    code splitting
    Dynamic Bundling
    lazy loading
    on demand loading
*/
const About = lazy(() => import("./Components/About.js"));

let toastIdCounter = 0;

const AppLayoutComponent = () =>
{
    const [toasts, setToasts] = useState([]);

    const pushToast = (message, variant = "default") =>
    {
        const id = ++toastIdCounter;
        setToasts((prev) => [...prev, {id, message, variant}]);

        setTimeout(() =>
        {
            setToasts((prev) => prev.filter((t) => t.id !== id));
        }, 4500);
    };

    return (
        <ToastContext.Provider value={{pushToast}}>
            <div className="app min-h-screen bg-paper">
                <Header/>
                <Outlet/>
                <ToastStack toasts={toasts}/>
            </div>
        </ToastContext.Provider>
    );
};

const AppRouter = createBrowserRouter(
    [
        {
            path: "/",
            element: <AppLayoutComponent/>,
            children: [
            {
                path: "/",
                element: <Dashboard/>,
            },
            {
                path: "/pack",
                element: <Pack/>,
            },
            {
                path: "/unpack",
                element: <Unpack/>,
            },
            {
                path: "/history",
                element: <History/>,
            },
            {
                path: "/history/:id",
                element: <OperationDetails/>,
            },
            {
                path: "/about",
                element: <Suspense fallback={<h1>loading..</h1>}><About/></Suspense>,
            },
            ],
            errorElement: <Error/>,
        },

    ]
);

const root = ReactDOM.createRoot(document.getElementById("root"));

root.render(<RouterProvider router={AppRouter}/>);
