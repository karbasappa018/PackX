import { useContext, useState } from "react";
import FileDropzone from "./FileDropzone";
import FileList from "./FileList";
import ToastContext from "../utils/ToastContext";
import { PACK_API, OPERATIONS_API } from "../utils/constants";
import { formatBytes } from "../utils/formatUtils";

const Pack = () =>
{
    const [files, setFiles] = useState([]);
    const [packName, setPackName] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);

    const {pushToast} = useContext(ToastContext);

    const addFiles = (incoming) =>
    {
        setResult(null);

        setFiles((prev) =>
        {
            const existingNames = new Set(prev.map((f) => f.name.toLowerCase()));
            const merged = [...prev];

            for (const f of incoming)
            {
                if (!existingNames.has(f.name.toLowerCase()))
                {
                    merged.push(f);
                    existingNames.add(f.name.toLowerCase());
                }
            }

            return merged;
        });
    };

    const removeFile = (index) =>
    {
        setFiles((prev) => prev.filter((_, i) => i !== index));
    };

    const handlePack = async () =>
    {
        if (files.length === 0)
        {
            pushToast("Select at least one .txt file to pack", "error");
            return;
        }
        if (!packName.trim())
        {
            pushToast("Give the packed file a name", "error");
            return;
        }

        const formData = new FormData();
        files.forEach((file) => formData.append("files", file));
        formData.append("packName", packName.trim());

        setSubmitting(true);

        try
        {
            const response = await fetch(PACK_API, {
                method: "POST",
                body: formData,
            });

            if (!response.ok)
            {
                const errJson = await response.json();
                throw new Error(errJson.message || "Packing failed");
            }

            const json = await response.json();
            setResult(json);
            pushToast("Packing completed successfully", "success");
        }
        catch (err)
        {
            pushToast(err.message, "error");
        }
        finally
        {
            setSubmitting(false);
        }
    };

    const totalSize = files.reduce((sum, f) => sum + f.size, 0);

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h1 className="text-2xl font-semibold text-ink-900 mb-1">Pack Files</h1>
            <p className="text-ink-500 mb-6">
                Files are combined into a single archive: a 100-byte header per file, followed by its
                XOR-transformed bytes.
            </p>

            <div className="border border-line rounded bg-white p-5 mb-4">
                <FileDropzone onFilesSelected={addFiles} accept=".txt" multiple={true}/>
                <FileList files={files} onRemove={removeFile}/>

                {files.length > 0 && (
                    <p className="font-mono text-xs text-ink-500 mt-3">
                        {files.length} file{files.length === 1 ? "" : "s"} selected · {formatBytes(totalSize)} total
                    </p>
                )}
            </div>

            <div className="border border-line rounded bg-white p-5 mb-4">
                <label className="block font-mono text-xs text-ink-500 mb-2" htmlFor="packName">
                    Packed file name
                </label>
                <input
                    id="packName"
                    type="text"
                    placeholder="my-archive"
                    value={packName}
                    onChange={(e) => setPackName(e.target.value)}
                    className="w-full border border-line rounded px-3 py-2 font-mono text-sm mb-4"
                />

                <button
                    className="px-4 py-2 rounded bg-amber-500 text-ink-900 font-semibold text-sm disabled:opacity-50"
                    onClick={handlePack}
                    disabled={submitting}
                >
                    {submitting ? "Packing..." : "Pack Files"}
                </button>
            </div>

            {result && (
                <div className="border border-line rounded bg-white">
                    <div className="flex justify-between items-center px-5 py-3 border-b border-line">
                        <h3 className="font-semibold">Packing completed successfully</h3>
                        <span className="px-2 py-0.5 rounded bg-forest-100 text-forest-600 text-xs font-mono uppercase">
                            SUCCESS
                        </span>
                    </div>
                    <div className="p-5 font-mono text-sm">
                        <p className="mb-1">Files packed: {result.numberOfFiles}</p>
                        <p className="mb-4">Total size: {formatBytes(result.totalSize)}</p>
                        <a
                            className="inline-block px-4 py-2 rounded bg-ink-900 text-paper font-semibold text-sm"
                            href={OPERATIONS_API + "/" + result.id + "/download"}
                        >
                            Download Packed File
                        </a>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Pack;
