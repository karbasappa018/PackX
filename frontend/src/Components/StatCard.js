const StatCard = ({label, value}) =>
{
    return (
        <div className="border border-line rounded p-4 bg-white">
            <p className="font-mono text-xs text-ink-500 mb-2">{label}</p>
            <p className="text-2xl font-semibold text-ink-900">{value}</p>
        </div>
    );
};

export default StatCard;
