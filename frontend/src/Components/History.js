import Shimmer from "./Shimmer";
import OperationRow from "./OperationRow";
import useOperations from "../utils/useOperations";

const History = () =>
{
    const [operations] = useOperations();

    if (operations === null)
    {
        return <Shimmer/>;
    }

    return (
        <div className="p-6 max-w-5xl mx-auto">
            <h1 className="text-2xl font-semibold text-ink-900 mb-1">History</h1>
            <p className="text-ink-500 mb-6">Every pack and unpack operation, most recent first.</p>

            <div className="border border-line rounded bg-white">
                {operations.length === 0 && (
                    <p className="text-center text-ink-500 p-10">No operations recorded yet.</p>
                )}

                {operations.map((operation) => (
                    <OperationRow key={operation.id} operation={operation}/>
                ))}
            </div>
        </div>
    );
};

export default History;
