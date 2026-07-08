import { useEffect, useState } from "react";
import { DASHBOARD_STATS_API } from "./constants";

const useDashboardStats = () =>
{
    const [stats, setStats] = useState(null);

    useEffect(() =>
    {
        fetchStats();
    }, []);

    const fetchStats = async () =>
    {
        const response = await fetch(DASHBOARD_STATS_API);
        const json = await response.json();

        setStats(json);
    };

    return stats;
};

export default useDashboardStats;
