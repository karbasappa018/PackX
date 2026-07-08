export const formatBytes = (bytes) =>
{
    if (bytes === undefined || bytes === null) return "—";
    if (bytes === 0) return "0 B";

    const units = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    const value = bytes / Math.pow(1024, i);

    return value.toFixed(i === 0 ? 0 : 1) + " " + units[i];
};

export const formatDate = (isoString) =>
{
    if (!isoString) return "—";

    const d = new Date(isoString);
    return d.toLocaleString(undefined, {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
};

export const formatDuration = (ms) =>
{
    if (ms === undefined || ms === null) return "—";
    if (ms < 1000) return ms + " ms";

    return (ms / 1000).toFixed(2) + " s";
};
