import { useRouteError } from "react-router-dom";

const Error = () =>
{
    const err = useRouteError();
    console.log(err);

    return (
        <div className="text-center p-20">
            <h1 className="text-2xl font-bold">Oops!!!</h1>
            <h2 className="text-ink-500 my-2">Something Went Wrong</h2>
            <h3 className="font-mono text-sm">{err?.status}: {err?.statusText}</h3>
        </div>
    );
};

export default Error;
