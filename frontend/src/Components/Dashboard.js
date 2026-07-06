import { Link } from "react-router-dom";
import Shimmer from "./Shimmer";
import StatCard from "./StatCard";
import OperationRow from "./OperationRow";
import useDashboardStats from "../utils/useDashboardStats";
import { formatBytes } from "../utils/formatUtils";

const Dashboard = () =>
{
    const stats = useDashboardStats();

    if (stats === null)
    {
        return <Shimmer/>;
    }

    return (
        <div className="p-6 max-w-5xl mx-auto">
            <h1 className="text-2xl font-semibold text-ink-900 mb-1">Dashboard</h1>
            <p className="text-ink-500 mb-6">A running ledger of every pack and unpack operation.</p>

            <div className="grid grid-cols-4 gap-3 mb-3">
                <StatCard label="Total Operations" value={stats.totalOperations}/>
                <StatCard label="Total Packs" value={stats.totalPacks}/>
                <StatCard label="Total Unpacks" value={stats.totalUnpacks}/>
                <StatCard label="Data Processed" value={formatBytes(stats.totalDataProcessedBytes)}/>
            </div>

            <div className="grid grid-cols-2 gap-3 mb-6">
                <StatCard label="Files Packed" value={stats.filesPacked}/>
                <StatCard label="Files Unpacked" value={stats.filesUnpacked}/>
            </div>

            <div className="border border-line rounded bg-white">
                <div className="flex justify-between items-center px-4 py-3 border-b border-line">
                    <h3 className="font-semibold">Recent Operations</h3>
                    <Link to="/history" className="font-mono text-xs text-amber-600 hover:underline">
                        View all
                    </Link>
                </div>

                {stats.recentOperations.length === 0 && (
                    <p className="text-center text-ink-500 p-10">
                        No operations yet. Pack or unpack a file to see it here.
                    </p>
                )}

                {stats.recentOperations.map((operation) => (
                    <OperationRow key={operation.id} operation={operation}/>
                ))}
            </div>
        </div>
    );
};

export default Dashboard;
