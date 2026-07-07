import { formatBytes } from "../utils/formatUtils";

const FileList = ({files, onRemove}) =>
{
    if (files.length === 0) return null;

    return (
        <ul className="mt-4 border-t border-line">
            {files.map((file, index) => (
                <li
                    key={file.name + "-" + index}
                    className="flex items-center gap-3 py-2 border-b border-line font-mono text-sm"
                >
                    <span className="flex-1 truncate">{file.name}</span>
                    <span className="text-ink-500">{formatBytes(file.size)}</span>
                    <span>✓</span>
                    <button
                        type="button"
                        className="text-rust-600 text-xs"
                        onClick={() => onRemove(index)}
                    >
                        remove
                    </button>
                </li>
            ))}
        </ul>
    );
};

export default FileList;
