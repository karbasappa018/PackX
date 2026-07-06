const About = () =>
{
    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h1 className="text-2xl font-semibold text-ink-900 mb-1">About PackX</h1>
            <p className="text-ink-500 mb-6">
                A web platform built on top of a much smaller idea: two Java console programs that packed
                and unpacked .txt files with a single-byte XOR cipher.
            </p>

            <div className="border border-line rounded bg-white mb-4">
                <div className="px-5 py-3 border-b border-line">
                    <h3 className="font-semibold">How packing works</h3>
                </div>
                <div className="p-5 text-sm leading-relaxed">
                    Each selected file is written as a 100-byte header — its name and size, space-padded —
                    followed immediately by its contents, transformed one byte at a time against a fixed
                    key. Multiple files are concatenated the same way, one header-and-payload pair after
                    another.
                </div>
            </div>

            <div className="border border-line rounded bg-white mb-4">
                <div className="px-5 py-3 border-b border-line">
                    <h3 className="font-semibold">How unpacking works</h3>
                </div>
                <div className="p-5 text-sm leading-relaxed">
                    The unpacker reads 100 bytes at a time, parses a filename and size from each header,
                    reads that many bytes back out, reverses the transform, and writes a new file. When
                    more than one file comes out, they're bundled into a ZIP for download.
                </div>
            </div>

            <div className="border border-line rounded bg-white">
                <div className="px-5 py-3 border-b border-line">
                    <h3 className="font-semibold">A note on the cipher</h3>
                </div>
                <div className="p-5 text-sm leading-relaxed">
                    The XOR transform (key <span className="font-mono">0x11</span>) is kept for
                    compatibility with the original console programs it descends from. It is not real
                    encryption — it offers no meaningful confidentiality and should never be relied on for
                    sensitive data.
                </div>
            </div>
        </div>
    );
};

export default About;
