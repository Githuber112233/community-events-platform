export interface Event {
  id: number;
  title: string;
  description: string;
  category: string;
  date: string;
  time: string;
  location: string;
  organizer: {
    name: string;
    avatar: string;
    id?: number;
  };
  participants: number;
  maxParticipants: number;
  images: string[];
  isHot?: boolean;
  isNew?: boolean;
}

export interface DisplayEvent extends Event {
  [key: string]: any
}

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  bio?: string;
  location?: string;
  joinedDate: string;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
}
