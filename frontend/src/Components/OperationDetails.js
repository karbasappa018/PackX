import { useContext } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Shimmer from "./Shimmer";
import ToastContext from "../utils/ToastContext";
import useOperation from "../utils/useOperation";
import { OPERATIONS_API } from "../utils/constants";
import { formatBytes, formatDate, formatDuration } from "../utils/formatUtils";

const statusPillClass = (status) =>
{
    if (status === "SUCCESS") return "bg-forest-100 text-forest-600";
    if (status === "FAILED") return "bg-rust-100 text-rust-600";
    return "bg-amber-100 text-amber-600";
};

const OperationDetails = () =>
{
    const {id} = useParams();
    const operation = useOperation(id);

    const navigate = useNavigate();
    const {pushToast} = useContext(ToastContext);

    if (operation === null)
    {
        return <Shimmer/>;
    }

    const handleDelete = async () =>
    {
        const response = await fetch(OPERATIONS_API + "/" + id, {method: "DELETE"});

        if (response.ok)
        {
            pushToast("Operation deleted", "success");
            navigate("/history");
        }
        else
        {
            pushToast("Could not delete operation", "error");
        }
    };

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h1 className="text-2xl font-semibold text-ink-900 mb-1">Operation Details</h1>
            <p className="font-mono text-xs text-ink-500 mb-6">{operation.id}</p>

            <div className="border border-line rounded bg-white">
                <div className="flex justify-between items-center px-5 py-3 border-b border-line">
                    <h3 className="font-semibold">
                        {operation.operationType === "PACK" ? "Pack operation" : "Unpack operation"}
                    </h3>
                    <span className={"px-2 py-0.5 rounded text-xs font-mono uppercase " + statusPillClass(operation.status)}>
                        {operation.status}
                    </span>
                </div>

                <div className="grid grid-cols-2 border-b border-line">
                    <div className="p-4 border-r border-b border-line">
                        <p className="font-mono text-xs text-ink-500 mb-1">Type</p>
                        <p className="font-mono text-sm">{operation.operationType}</p>
                    </div>
                    <div className="p-4 border-b border-line">
                        <p className="font-mono text-xs text-ink-500 mb-1">Date</p>
                        <p className="font-mono text-sm">{formatDate(operation.createdAt)}</p>
                    </div>
                    <div className="p-4 border-r border-b border-line">
                        <p className="font-mono text-xs text-ink-500 mb-1">Number of files</p>
                        <p className="font-mono text-sm">{operation.numberOfFiles}</p>
                    </div>
                    <div className="p-4 border-b border-line">
                        <p className="font-mono text-xs text-ink-500 mb-1">Total size</p>
                        <p className="font-mono text-sm">{formatBytes(operation.totalSize)}</p>
                    </div>
                    <div className="p-4 border-r border-b border-line">
                        <p className="font-mono text-xs text-ink-500 mb-1">Input filename</p>
                        <p className="font-mono text-sm break-all">{operation.inputFileName || "—"}</p>
                    </div>
                    <div className="p-4 border-b border-line">
                        <p className="font-mono text-xs text-ink-500 mb-1">Output filename</p>
                        <p className="font-mono text-sm break-all">{operation.outputFileName || "—"}</p>
                    </div>
                    <div className="p-4 border-r">
                        <p className="font-mono text-xs text-ink-500 mb-1">Processing time</p>
                        <p className="font-mono text-sm">{formatDuration(operation.processingTimeMs)}</p>
                    </div>
                    <div className="p-4">
                        <p className="font-mono text-xs text-ink-500 mb-1">Completed at</p>
                        <p className="font-mono text-sm">{formatDate(operation.completedAt)}</p>
                    </div>
                </div>

                {operation.status === "FAILED" && operation.errorMessage && (
                    <div className="p-5">
                        <div className="border border-rust-600 bg-rust-100 rounded p-3 text-sm">
                            {operation.errorMessage}
                        </div>
                    </div>
                )}

                {operation.fileNames && operation.fileNames.length > 0 && (
                    <div className="px-5 pb-2">
                        <p className="font-mono text-xs text-ink-500 mb-2 mt-4">Individual files</p>
                        <ul className="border-t border-line">
                            {operation.fileNames.map((name) => (
                                <li key={name} className="font-mono text-sm py-2 border-b border-line">{name}</li>
                            ))}
                        </ul>
                    </div>
                )}

                {operation.status === "SUCCESS" && (
                    <div className="p-5 flex gap-3">
                        <a
                            className="px-4 py-2 rounded bg-ink-900 text-paper font-semibold text-sm"
                            href={OPERATIONS_API + "/" + operation.id + "/download"}
                        >
                            Download
                        </a>
                        <button
                            className="px-4 py-2 rounded border border-rust-600 text-rust-600 font-semibold text-sm"
                            onClick={handleDelete}
                        >
                            Delete operation
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default OperationDetails;
