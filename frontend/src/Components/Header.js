import { Link } from "react-router-dom";

const Header = () =>
{
    return (
        <div className="flex justify-between items-center bg-ink-900 text-paper px-6 py-4 shadow-lg">
            <div className="flex items-baseline gap-2">
                <span className="font-mono text-amber-500 text-lg font-semibold">0x11</span>
                <span className="text-lg font-semibold">PackX</span>
            </div>

            <ul className="flex items-center gap-6 font-mono text-sm">
                <li>
                    <Link to="/" className="hover:text-amber-500">Dashboard</Link>
                </li>
                <li>
                    <Link to="/pack" className="hover:text-amber-500">Pack Files</Link>
                </li>
                <li>
                    <Link to="/unpack" className="hover:text-amber-500">Unpack Files</Link>
                </li>
                <li>
                    <Link to="/history" className="hover:text-amber-500">History</Link>
                </li>
                <li>
                    <Link to="/about" className="hover:text-amber-500">About</Link>
                </li>
            </ul>
        </div>
    );
};

export default Header;
