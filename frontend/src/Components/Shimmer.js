const Shimmer = () =>
{
    return (
        <div className="p-6">
            <div className="h-6 w-48 bg-paper-200 rounded animate-pulse mb-6"></div>

            <div className="grid grid-cols-4 gap-3 mb-6">
                <div className="h-20 bg-paper-200 rounded animate-pulse"></div>
                <div className="h-20 bg-paper-200 rounded animate-pulse"></div>
                <div className="h-20 bg-paper-200 rounded animate-pulse"></div>
                <div className="h-20 bg-paper-200 rounded animate-pulse"></div>
            </div>

            <div className="h-10 bg-paper-200 rounded animate-pulse mb-2"></div>
            <div className="h-10 bg-paper-200 rounded animate-pulse mb-2"></div>
            <div className="h-10 bg-paper-200 rounded animate-pulse mb-2"></div>
        </div>
    );
};

export default Shimmer;
