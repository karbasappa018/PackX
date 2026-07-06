import { useRef, useState } from "react";

const FileDropzone = ({onFilesSelected, accept, multiple}) =>
{
    const [active, setActive] = useState(false);
    const inputRef = useRef(null);

    const handleDrop = (e) =>
    {
        e.preventDefault();
        setActive(false);

        const files = Array.from(e.dataTransfer.files || []);
        if (files.length) onFilesSelected(files);
    };

    const handleBrowse = (e) =>
    {
        const files = Array.from(e.target.files || []);
        if (files.length) onFilesSelected(files);

        e.target.value = "";
    };

    return (
        <div
            className={
                "border-2 border-dashed rounded p-10 text-center " +
                (active ? "border-amber-600 bg-amber-100" : "border-line bg-paper")
            }
            onDragOver={(e) =>
            {
                e.preventDefault();
                setActive(true);
            }}
            onDragLeave={() => setActive(false)}
            onDrop={handleDrop}
        >
            <p className="font-semibold mb-1">Drag files here</p>
            <p className="text-ink-500 text-sm mb-4">or choose them from your computer · {accept} only</p>

            <button
                type="button"
                className="px-4 py-2 border border-line rounded font-semibold text-sm hover:bg-paper-100"
                onClick={() => inputRef.current.click()}
            >
                Browse files
            </button>

            <input
                ref={inputRef}
                type="file"
                accept={accept}
                multiple={multiple}
                onChange={handleBrowse}
                className="hidden"
            />
        </div>
    );
};

export default FileDropzone;
