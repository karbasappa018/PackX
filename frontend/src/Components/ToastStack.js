const toastVariantClass = (variant) =>
{
    if (variant === "error") return "border-l-rust-600";
    if (variant === "success") return "border-l-forest-600";

    return "border-l-amber-500";
};

const ToastStack = ({toasts}) =>
{
    return (
        <div className="fixed bottom-6 right-6 flex flex-col gap-2 z-50">
            {toasts.map((t) => (
                <div
                    key={t.id}
                    className={
                        "bg-ink-900 text-paper px-4 py-3 rounded text-sm border-l-4 min-w-[240px] shadow-lg " +
                        toastVariantClass(t.variant)
                    }
                >
                    {t.message}
                </div>
            ))}
        </div>
    );
};

export default ToastStack;
