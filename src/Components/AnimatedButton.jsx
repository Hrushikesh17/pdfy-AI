import React from "react";
import styled from "styled-components";

const StyledButton = styled.button`
  position: relative;
  margin: 1rem .5rem 0rem 0rem;
  padding: 12px 18px;
  border-radius: 8px;
  border: 1px solid ${(props) => (props.$primary ? "#3498db" : "#2ecc71")};
  font-size: .7rem;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 2px;
  background: transparent;
  color: green;
  overflow: hidden;
  box-shadow: 0 0 0 0 transparent;
  transition: all 0.3s ease-in-out;
  cursor: pointer;

  &:hover {
    background: ${(props) => (props.$primary ? "#3498db" : "#2ecc71")};
    box-shadow: 0 0 30px 5px rgba(0, 142, 236, 0.815);
  }

  &::before {
    content: '';
    display: block;
    width: 0px;
    height: 86%;
    position: absolute;
    top: 7%;
    left: 0%;
    opacity: 0;
    background: #fff;
    box-shadow: 0 0 50px 30px #fff;
    transform: skewX(-20deg);
  }

  &:hover::before {
    animation: shimmer 0.5s linear;
  }

  @keyframes shimmer {
    from {
      opacity: 0;
      left: 0%;
    }
    50% {
      opacity: 1;
    }
    to {
      opacity: 0;
      left: 100%;
    }
  }

  &:active {
    box-shadow: 0 0 0 0 transparent;
    transition: box-shadow 0.2s ease-in;
  }
`;

const AnimatedButton = ({ children, onClick, primary }) => {
  return (
    <StyledButton onClick={onClick} $primary={primary}>
      {children}
    </StyledButton>
  );
};

export default AnimatedButton;