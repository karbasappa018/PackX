import { useEffect, useState } from "react";
import { OPERATIONS_API } from "./constants";

const useOperation = (operationId) =>
{
    const [operation, setOperation] = useState(null);

    useEffect(() =>
    {
        fetchOperation();
    }, [operationId]);

    const fetchOperation = async () =>
    {
        const response = await fetch(OPERATIONS_API + "/" + operationId);
        const json = await response.json();

        setOperation(json);
    };

    return operation;
};

export default useOperation;
