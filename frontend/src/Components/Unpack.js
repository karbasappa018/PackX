import { useContext, useState } from "react";
import FileDropzone from "./FileDropzone";
import ToastContext from "../utils/ToastContext";
import { UNPACK_API, OPERATIONS_API } from "../utils/constants";

const Unpack = () =>
{
    const [file, setFile] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);

    const {pushToast} = useContext(ToastContext);

    const handleSelect = (incoming) =>
    {
        setResult(null);
        setFile(incoming[0]);
    };

    const handleUnpack = async () =>
    {
        if (!file)
        {
            pushToast("Upload a packed file first", "error");
            return;
        }

        const formData = new FormData();
        formData.append("file", file);

        setSubmitting(true);

        try
        {
            const response = await fetch(UNPACK_API, {
                method: "POST",
                body: formData,
            });

            if (!response.ok)
            {
                const errJson = await response.json();
                throw new Error(errJson.message || "Unpacking failed");
            }

            const json = await response.json();
            setResult(json);
            pushToast("Unpacking completed successfully", "success");
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

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h1 className="text-2xl font-semibold text-ink-900 mb-1">Unpack Files</h1>
            <p className="text-ink-500 mb-6">
                Upload a .pack file. PackX reads each 100-byte header, decrypts the following bytes, and
                recreates the original files.
            </p>

            <div className="border border-line rounded bg-white p-5 mb-4">
                <FileDropzone onFilesSelected={handleSelect} accept=".pack" multiple={false}/>

                {file && (
                    <p className="font-mono text-sm mt-3">Selected: {file.name}</p>
                )}

                <div className="mt-4">
                    <button
                        className="px-4 py-2 rounded bg-amber-500 text-ink-900 font-semibold text-sm disabled:opacity-50"
                        onClick={handleUnpack}
                        disabled={submitting}
                    >
                        {submitting ? "Unpacking..." : "Unpack"}
                    </button>
                </div>
            </div>

            {result && (
                <div className="border border-line rounded bg-white">
                    <div className="flex justify-between items-center px-5 py-3 border-b border-line">
                        <h3 className="font-semibold">Unpacking completed successfully</h3>
                        <span className="px-2 py-0.5 rounded bg-forest-100 text-forest-600 text-xs font-mono uppercase">
                            SUCCESS
                        </span>
                    </div>

                    <div className="p-5 font-mono text-sm">
                        <p className="mb-1">Files extracted: {result.numberOfFiles}</p>
                        <p className="mb-4">Output: {result.outputFileName}</p>

                        {result.fileNames && result.fileNames.length > 0 && (
                            <ul className="mb-4 border-t border-line">
                                {result.fileNames.map((name) => (
                                    <li key={name} className="py-2 border-b border-line">{name}</li>
                                ))}
                            </ul>
                        )}

                        <a
                            className="inline-block px-4 py-2 rounded bg-ink-900 text-paper font-semibold text-sm"
                            href={OPERATIONS_API + "/" + result.id + "/download"}
                        >
                            {result.numberOfFiles > 1 ? "Download All Files (ZIP)" : "Download File"}
                        </a>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Unpack;
