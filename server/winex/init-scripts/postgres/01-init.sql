-- ============================================
-- WinexPlay PostgreSQL Database Schema
-- Version: 1.0.0
-- ============================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- USERS & AUTHENTICATION
-- ============================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255), -- NULL for OAuth-only users
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar_url VARCHAR(500),
    
    -- OAuth
    google_id VARCHAR(255) UNIQUE,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, BANNED
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_google_id ON users(google_id);
CREATE INDEX idx_users_status ON users(status);

-- ============================================
-- WALLET & TRANSACTIONS
-- ============================================

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- Bonus balance (wagering requirements)
    bonus_balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT positive_balance CHECK (balance >= 0),
    CONSTRAINT positive_bonus CHECK (bonus_balance >= 0)
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    
    type VARCHAR(30) NOT NULL, -- DEPOSIT, WITHDRAWAL, BET_STAKE, BET_WIN, BET_REFUND, BONUS
    amount DECIMAL(18, 2) NOT NULL,
    balance_before DECIMAL(18, 2) NOT NULL,
    balance_after DECIMAL(18, 2) NOT NULL,
    
    -- Reference to related entity
    reference_type VARCHAR(30), -- BET, DEPOSIT_REQUEST, etc.
    reference_id UUID,
    
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- PENDING, COMPLETED, FAILED, CANCELLED
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_reference ON transactions(reference_type, reference_id);

-- ============================================
-- SPORTS & EVENTS
-- ============================================

CREATE TABLE sports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) NOT NULL UNIQUE, -- FOOTBALL, BASKETBALL, TENNIS
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE leagues (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sport_id UUID NOT NULL REFERENCES sports(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    country VARCHAR(100),
    logo_url VARCHAR(500),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leagues_sport_id ON leagues(sport_id);

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sport_id UUID NOT NULL REFERENCES sports(id),
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(50),
    logo_url VARCHAR(500),
    country VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_teams_sport_id ON teams(sport_id);

CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    external_id VARCHAR(100) UNIQUE, -- ID from AI Sports Agent
    sport_id UUID NOT NULL REFERENCES sports(id),
    league_id UUID REFERENCES leagues(id),
    
    home_team_id UUID REFERENCES teams(id),
    away_team_id UUID REFERENCES teams(id),
    home_team_name VARCHAR(200) NOT NULL,
    away_team_name VARCHAR(200) NOT NULL,
    
    -- Current state
    status VARCHAR(20) NOT NULL DEFAULT 'PREMATCH', -- PREMATCH, LIVE, HALFTIME, FINISHED, CANCELLED, SUSPENDED
    score_home INT DEFAULT 0,
    score_away INT DEFAULT 0,
    current_minute INT DEFAULT 0,
    last_event VARCHAR(500),
    
    -- Scheduling
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_start_time ON matches(start_time);
CREATE INDEX idx_matches_sport_league ON matches(sport_id, league_id);
CREATE INDEX idx_matches_external_id ON matches(external_id);

-- ============================================
-- MARKETS & ODDS
-- ============================================

CREATE TABLE markets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    
    market_type VARCHAR(50) NOT NULL, -- MATCH_WINNER, OVER_UNDER, HANDICAP, BOTH_TEAMS_SCORE
    name VARCHAR(200) NOT NULL,
    line DECIMAL(5, 2), -- For handicap/over-under markets
    
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, SUSPENDED, SETTLED, VOID
    result_selection_id UUID, -- Winner selection after settlement
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_markets_match_id ON markets(match_id);
CREATE INDEX idx_markets_status ON markets(status);

CREATE TABLE selections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    market_id UUID NOT NULL REFERENCES markets(id) ON DELETE CASCADE,
    
    code VARCHAR(20) NOT NULL, -- 1, X, 2, over, under
    name VARCHAR(200) NOT NULL,
    odds DECIMAL(10, 2) NOT NULL,
    
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, WINNER, LOSER, VOID
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_selections_market_id ON selections(market_id);

-- ============================================
-- BETS
-- ============================================

CREATE TABLE bets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    
    bet_type VARCHAR(20) NOT NULL, -- SINGLE, ACCUMULATOR
    stake DECIMAL(18, 2) NOT NULL,
    total_odds DECIMAL(10, 4) NOT NULL,
    potential_win DECIMAL(18, 2) NOT NULL,
    
    -- Result
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, WON, LOST, VOID, CASHED_OUT
    actual_win DECIMAL(18, 2),
    settled_at TIMESTAMP WITH TIME ZONE,
    
    -- Cash out
    cashout_available BOOLEAN NOT NULL DEFAULT FALSE,
    cashout_amount DECIMAL(18, 2),
    cashed_out_at TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bets_user_id ON bets(user_id);
CREATE INDEX idx_bets_status ON bets(status);
CREATE INDEX idx_bets_created_at ON bets(created_at DESC);

CREATE TABLE bet_selections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bet_id UUID NOT NULL REFERENCES bets(id) ON DELETE CASCADE,
    
    match_id UUID NOT NULL REFERENCES matches(id),
    market_id UUID NOT NULL REFERENCES markets(id),
    selection_id UUID NOT NULL REFERENCES selections(id),
    
    odds_at_placement DECIMAL(10, 2) NOT NULL,
    
    -- Result for this leg
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, WON, LOST, VOID
    settled_at TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bet_selections_bet_id ON bet_selections(bet_id);
CREATE INDEX idx_bet_selections_match_id ON bet_selections(match_id);

-- ============================================
-- LEADERBOARD (Materialized for performance)
-- ============================================

CREATE TABLE leaderboard_snapshot (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    username VARCHAR(50) NOT NULL,
    
    -- Stats
    total_bets INT NOT NULL DEFAULT 0,
    total_wins INT NOT NULL DEFAULT 0,
    total_stake DECIMAL(18, 2) NOT NULL DEFAULT 0,
    total_winnings DECIMAL(18, 2) NOT NULL DEFAULT 0,
    profit DECIMAL(18, 2) NOT NULL DEFAULT 0,
    win_rate DECIMAL(5, 2) NOT NULL DEFAULT 0,
    
    -- Ranking
    rank INT,
    period VARCHAR(20) NOT NULL, -- DAILY, WEEKLY, MONTHLY, ALL_TIME
    period_start DATE NOT NULL,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, period, period_start)
);

CREATE INDEX idx_leaderboard_period ON leaderboard_snapshot(period, period_start, rank);
CREATE INDEX idx_leaderboard_user ON leaderboard_snapshot(user_id);

-- ============================================
-- REFRESH TOKENS (for JWT)
-- ============================================

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Device tracking
    device_info VARCHAR(500),
    ip_address VARCHAR(50)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- ============================================
-- SEED DATA
-- ============================================

-- Sports
INSERT INTO sports (code, name, icon, display_order) VALUES
    ('FOOTBALL', 'Football', '⚽', 1),
    ('BASKETBALL', 'Basketball', '🏀', 2),
    ('TENNIS', 'Tennis', '🎾', 3),
    ('HOCKEY', 'Ice Hockey', '🏒', 4),
    ('BASEBALL', 'Baseball', '⚾', 5);

-- Example leagues
INSERT INTO leagues (sport_id, code, name, country, display_order) VALUES
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'UCL', 'UEFA Champions League', 'Europe', 1),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'EPL', 'English Premier League', 'England', 2),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'LALIGA', 'La Liga', 'Spain', 3),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'SERIEA', 'Serie A', 'Italy', 4),
    ((SELECT id FROM sports WHERE code = 'BASKETBALL'), 'NBA', 'NBA', 'USA', 1);

-- Example teams (for AI Agent to reference)
INSERT INTO teams (sport_id, name, short_name, country) VALUES
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Real Madrid', 'RMA', 'Spain'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Barcelona', 'BAR', 'Spain'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Arsenal', 'ARS', 'England'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Manchester City', 'MCI', 'England'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Paris Saint-Germain', 'PSG', 'France'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'AC Milan', 'MIL', 'Italy'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Inter Milan', 'INT', 'Italy'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Napoli', 'NAP', 'Italy'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Chelsea', 'CHE', 'England'),
    ((SELECT id FROM sports WHERE code = 'FOOTBALL'), 'Bayern Munich', 'BAY', 'Germany');

-- ============================================
-- FUNCTIONS
-- ============================================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to all tables with updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_matches_updated_at BEFORE UPDATE ON matches 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_markets_updated_at BEFORE UPDATE ON markets 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_selections_updated_at BEFORE UPDATE ON selections 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_bets_updated_at BEFORE UPDATE ON bets 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
