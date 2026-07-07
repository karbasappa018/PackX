import { Link } from "react-router-dom";
import { formatBytes, formatDate } from "../utils/formatUtils";

const statusPillClass = (status) =>
{
    if (status === "SUCCESS") return "bg-forest-100 text-forest-600";
    if (status === "FAILED") return "bg-rust-100 text-rust-600";
    return "bg-amber-100 text-amber-600";
};

const OperationRow = ({operation}) =>
{
    return (
        <Link
            to={"/history/" + operation.id}
            className="flex items-center gap-4 px-4 py-3 border-b border-line hover:bg-paper-100 font-mono text-sm"
        >
            <span className="px-2 py-0.5 rounded bg-ink-800 text-paper text-xs uppercase">
                {operation.operationType}
            </span>
            <span className="flex-1 truncate text-ink-800">
                {operation.outputFileName || operation.inputFileName}
            </span>
            <span className="text-ink-500">{operation.numberOfFiles} files</span>
            <span className="text-ink-500">{formatBytes(operation.totalSize)}</span>
            <span className={"px-2 py-0.5 rounded text-xs uppercase " + statusPillClass(operation.status)}>
                {operation.status}
            </span>
            <span className="text-ink-500">{formatDate(operation.createdAt)}</span>
        </Link>
    );
};

export default OperationRow;
