import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { RiCloseLine, RiMenuLine } from 'react-icons/ri';

const Navbar = ({ auth, logout }) => {
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();

  const toggleMenu = () => setIsOpen(!isOpen);

  const handleLogout = () => {
    logout();
    navigate('/'); // Fixed: Use navigate directly, not navigate.push
  };

  const isAuthenticated = !!auth.token;
  const isRecruiter = auth.role === 'recruiter';

  // Determine the home route based on the user's role
  const homeRoute = isAuthenticated
    ? isRecruiter
      ? '/recruiter/welcome'
      : '/candidate/welcome'
    : '/';

  return (
    <nav>
      <div className="nav__header">
        <div className="nav__logo">
          <Link to={homeRoute} className="logo">
            Hire<span>Sphere</span>
          </Link>
        </div>
        <div className="nav__menu__btn" onClick={toggleMenu}>
          {isOpen ? <RiCloseLine /> : <RiMenuLine />}
        </div>
      </div>
      <ul className={`nav__links ${isOpen ? 'open' : ''}`}>
        {isAuthenticated ? (
          <>
            <li>
              <Link to={homeRoute}>Home</Link>
            </li>
            <li>
              <Link to="/profile">Profile</Link>
            </li>
            {isRecruiter ? (
              <>
                <li>
                  <Link to="/create-job">Create Job</Link>
                </li>
                <li>
                  <Link to="/my-jobs">My Jobs</Link>
                </li>
              </>
            ) : (
              <li>
                <Link to="/jobs">Jobs</Link>
              </li>
            )}
            <li>
              <button className="btn" onClick={handleLogout}>
                Logout
              </button>
            </li>
          </>
        ) : (
          <>
            <li>
              <Link to="/login">Login</Link>
            </li>
            <li>
              <Link to="/signup">Register</Link> {/* Fixed: Changed /register to /signup to match App.js */}
            </li>
          </>
        )}
      </ul>
    </nav>
  );
};

export default Navbar;