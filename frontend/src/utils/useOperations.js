import { useEffect, useState } from "react";
import { OPERATIONS_API } from "./constants";

const useOperations = () =>
{
    const [operations, setOperations] = useState(null);

    useEffect(() =>
    {
        fetchOperations();
    }, []);

    const fetchOperations = async () =>
    {
        const response = await fetch(OPERATIONS_API);
        const json = await response.json();

        setOperations(json);
    };

    return [operations, fetchOperations];
};

export default useOperations;
